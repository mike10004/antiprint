#!/usr/bin/env python

# Program that formats the unit test cases as a CSV. This is used for visual
# inspection of the different ways in which browser/OS/device combos are
# depicted by the window.navigator object.

import sys
import csv
import json
from argparse import ArgumentParser

if __name__ == '__main__':
    p = ArgumentParser()
    p.add_argument("files", nargs='+', help="json files that encode navigator objects")
    args = p.parse_args()
    testcases = []
    columns = set()
    for f in args.files:
        with open(f, 'r') as ifile:
            testcase = json.load(ifile)
            testcases.append(testcase)
            for k in testcase.keys():
                columns.add(k)
    columns = list(columns)
    columns.sort()
    csvout = csv.writer(sys.stdout)
    csvout.writerow(columns)
    for testcase in testcases:
        row = [testcase[k] if k in testcase else '' for k in columns]
        csvout.writerow(row)
