#!/usr/bin/env python3
"""
process_attendance.py

Usage:
  - Save the long input string into a file or pass it as an argument.
  - The script will:
      * parse records of the form: id|userId|epochMillis|isWorking|status|overtime|deduction|notes|...
      * remove any records that fall on Saturday
      * for each user, find the date range present (min..max) and ensure every weekday (Mon-Fri)
        in that range has at least one record; if a weekday is missing for that user, the script will
        add a synthetic record with isWorking=1 and status=WORKING (i.e. a YES).

Output:
  - By default the script prints processed records to stdout in the same pipe-separated format.
  - You can redirect to a file.

Example (PowerShell):
  $data = "13|7|1766255400000|1|WORKING|0.0|0.0|| 17|7|1766341800000|1|WORKING|0.0|0.0|| ..."
  python .\backend\scripts\process_attendance.py --inline "$data" > processed.txt

Notes:
  - Epoch milliseconds are interpreted as UTC timestamps when converting to dates.
  - The script does NOT try to merge existing duplicates; it only removes Saturdays and adds missing weekdays.
  - New synthetic records get id=0 and an extra note "ADDED_BY_SCRIPT" so you can spot them.

"""

import argparse
from datetime import datetime, date, timedelta, timezone
import sys


def parse_record(token):
    # Split by pipe. Allow trailing empty fields.
    parts = token.split('|')
    # Ensure at least 7 parts
    while len(parts) < 8:
        parts.append('')
    # Map fields (best-effort based on provided data)
    rec = {
        'raw_id': parts[0].strip(),
        'user_id': parts[1].strip(),
        'epoch_ms': parts[2].strip(),
        'is_working_flag': parts[3].strip(),
        'status': parts[4].strip(),
        'overtime': parts[5].strip(),
        'deduction': parts[6].strip(),
        'notes': parts[7].strip() if len(parts) > 7 else ''
    }
    # Convert epoch to date
    try:
        ms = int(rec['epoch_ms'])
        dt = datetime.fromtimestamp(ms / 1000.0, tz=timezone.utc)
        rec['date'] = dt.date()
    except Exception:
        rec['date'] = None
    return rec


def record_to_line(rec):
    # Compose back into pipe-separated string. Keep id as raw_id (may be '0' for synthetic)
    # Keep trailing pipes for compatibility
    parts = [
        str(rec.get('raw_id', '')),
        str(rec.get('user_id', '')),
        str(rec.get('epoch_ms', '')),
        str(rec.get('is_working_flag', '')),
        str(rec.get('status', '')),
        str(rec.get('overtime', '')),
        str(rec.get('deduction', '')),
        str(rec.get('notes', '')),
    ]
    return '|'.join(parts) + '||'


def daterange(start_date, end_date):
    d = start_date
    while d <= end_date:
        yield d
        d += timedelta(days=1)


def main():
    parser = argparse.ArgumentParser(description='Process attendance tokens: remove Saturdays, add missing weekdays per user')
    group = parser.add_mutually_exclusive_group(required=True)
    group.add_argument('--file', '-f', help='Path to a text file containing tokens separated by whitespace')
    group.add_argument('--inline', '-i', help='Pass the entire token string inline')
    parser.add_argument('--tz', default='UTC', help='Timezone for interpreting epoch millis (default: UTC)')
    parser.add_argument('--min-date', help='Optional: force minimum date (YYYY-MM-DD)')
    parser.add_argument('--max-date', help='Optional: force maximum date (YYYY-MM-DD)')
    args = parser.parse_args()

    if args.file:
        with open(args.file, 'r', encoding='utf-8') as fh:
            data = fh.read()
    else:
        data = args.inline

    # Split tokens by whitespace; tokens themselves contain pipes
    tokens = [t for t in data.replace('\n', ' ').split(' ') if t.strip()]

    records = []
    for t in tokens:
        rec = parse_record(t)
        # Only keep records with a parsed date
        if rec['date'] is not None:
            records.append(rec)
        else:
            print(f"Warning: skipping token with invalid epoch: {t}", file=sys.stderr)

    if not records:
        print("No valid records parsed.", file=sys.stderr)
        sys.exit(1)

    # Group by user_id
    users = {}
    for r in records:
        uid = r['user_id']
        users.setdefault(uid, []).append(r)

    # Remove Saturdays and collect cleaned records
    cleaned = []
    for uid, recs in users.items():
        for r in recs:
            dow = r['date'].weekday()  # Monday=0, Sunday=6; Saturday=5
            if dow == 5:
                # skip Saturday
                continue
            cleaned.append(r)

    # Re-group cleaned by user and find date ranges
    cleaned_by_user = {}
    for r in cleaned:
        cleaned_by_user.setdefault(r['user_id'], []).append(r)

    # Determine min/max date per user (or use provided global min/max)
    global_min = min(r['date'] for r in cleaned)
    global_max = max(r['date'] for r in cleaned)
    if args.min_date:
        global_min = max(global_min, datetime.fromisoformat(args.min_date).date())
    if args.max_date:
        global_max = min(global_max, datetime.fromisoformat(args.max_date).date())

    # For each user, add missing weekdays between their min and max (or global min/max)
    added = []
    for uid, recs in cleaned_by_user.items():
        user_dates = set(r['date'] for r in recs)
        user_min = min(user_dates)
        user_max = max(user_dates)
        # Use per-user range; if you prefer global use global_min/global_max
        start = user_min
        end = user_max
        for d in daterange(start, end):
            if d.weekday() >= 5:
                # skip Saturday(5) and Sunday(6)
                continue
            if d not in user_dates:
                # create synthetic record
                epoch_ms = int(datetime(d.year, d.month, d.day, 12, 0, tzinfo=timezone.utc).timestamp() * 1000)
                new_rec = {
                    'raw_id': '0',
                    'user_id': uid,
                    'epoch_ms': str(epoch_ms),
                    'is_working_flag': '1',
                    'status': 'WORKING',
                    'overtime': '0.0',
                    'deduction': '0.0',
                    'notes': 'ADDED_BY_SCRIPT',
                    'date': d
                }
                added.append(new_rec)

    all_final = cleaned + added

    # Sort output by user_id (numeric if possible) then date
    def sort_key(r):
        try:
            return (int(r['user_id']), r['date'])
        except Exception:
            return (r['user_id'], r['date'])

    all_final_sorted = sorted(all_final, key=sort_key)

    # Print counts
    print(f"Original tokens: {len(tokens)}", file=sys.stderr)
    print(f"Parsed records (with dates): {len(records)}", file=sys.stderr)
    print(f"After removing Saturdays: {len(cleaned)}", file=sys.stderr)
    print(f"Added missing weekday records: {len(added)}", file=sys.stderr)
    print(f"Final records: {len(all_final_sorted)}", file=sys.stderr)

    # Output lines
    for r in all_final_sorted:
        # ensure epoch_ms present (for original records it was string)
        if 'epoch_ms' not in r or r['epoch_ms'] == '':
            r['epoch_ms'] = str(int(datetime(r['date'].year, r['date'].month, r['date'].day, 12, 0, tzinfo=timezone.utc).timestamp() * 1000))
        print(record_to_line(r))


if __name__ == '__main__':
    main()
