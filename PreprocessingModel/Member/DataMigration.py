import os
import sys
import pandas as pd
import re
from fuzzywuzzy import process, fuzz
import numpy as np
from datetime import datetime
from pathlib import Path

desired_features_fuzzy = {
    'Name': [
        'Name',
        'Nm',
        'MN',
        'Memb',
        'Mem Name',
        'Members Name',
        'M Name',
        'MName',
        'Member',
        "Member's Name",
        'Full Name',
        'Member Name',
        'MembersName',
        'Person Name',
        'Client Name',
        'Customer Name',
        'Individual Name',
        'First Name',
        'Last Name',
        'Given Name',
        'Forename',
        'Forename',
        'Surname',
        'Family Name',
        'Member Full Name',
        'Person Full Name',
        'Member First Name',
        'Member Last Name',
        'Client Full Name',
        'Customer Full Name',
        'Individual Full Name'
    ],
    'Member Surname': [
        'M Surname',
        'Surname',
        'Member Surname',
        'Members Surname',
        "Member's Surname",
        'Last Name',
        'Surname',
        'Nick Name'
        'Nickname',
        'Family Name'
    ],
    'Admission Date': [
        'Admission Date',
        'AdmDate',
        'Adm Date',
        'Registration Date',
        'Enrollment Date',
        'Joining Date',
        'Start Date',
        'Begin Date',
        'Admit Date'
        'Admitdate'
    ],
    'Primary Product': [
        'Product',
        'Pr',
        'Pr Name',
        'Product Name',
        'Primary Product Name',
        'Primary Product',
        'Primary Product Id',
        'Product Type',
        'Main Product',
        'Key Product',
        'Lead Product'
    ],
    'Samity Name': [
        'Samity Name',
        'SamityName',
        'Samity',
        'Smt',
        'Group Name',
        'Association Name',
        'Cluster Name',
        'Community Name',
        'Team Name',
        'Center Name',
        'Center'
    ],
    'Samity Code': [
        'Samity Code',
        'Samity Code',
        'SCode',
        'S Code',
        'SamityId',
        'Samity Id',
        'Group Code',
        'Association Code',
        'Cluster Code',
        'Community Code',
        'Center Code'
    ],
    'Age': [
        'Age',
        'Age (years)',
        'Years Old',
        'Age in Years',
        'Client Age',
        'Customer Age',
        'Individual Age',
        'Present Age',
        'Current Age'
    ],
    'Date Of Birth': [
        'Date Of Birth',
        'Birthdate',
        'BirthDate',
        'DOB',
        'DOBirth',
        'Birth Date',
        'Client DOB',
        'Customer DOB',
        'Individual DOB'
    ],
    'Member Code': [
        'Member ID',
        'ID',
        'Original Member ID'
        'Member Code',
        'MCode',
        'M Code',
        'MID',
        'Registration Code',
        'Members Code',
        'Members ID',
        'User ID',
        'Client ID',
        'Customer ID',
        'Individual ID',
        'Code'
    ],
    'Village/Block Ward': [
        'Village Ward',
        'Village',
        'Vlg',
        'Ward',
        'Present Address',
        'Permanent Address',
        'Present Village Ward',
        'Permanent Village Ward',
        'Ward',
        'Street',
        'Village Area',
        'Village Name',
        'Locality',
        'Block Ward',
        'Neighborhood',
        'Residential Area'
    ],
    'Post Office': [
        'Post Office',
        'Post',
        'Post Area',
        'Post Address',
        'Postal Office',
        'Postal Area',
        'Postal Address',
        'Mail Office',
        'Mail Area',
        'Mailing Area',
        'Mailing Address',
        'Present Post Office Area',
        'Permanent Post Office Area'
    ],
    'Gender': [
        'Gender',
        'Sex',
        'Male/Female',
        'Identity',
        'Client Gender',
        'Customer Gender',
        'Individual Gender'
    ],
    'Father Name': [
        'Fathers Name',
        "Father's Name",
        'Father',
        'Fathers',
        'Dad',
        'Paternal Authority',
        'Paternal',
        'Parent Name (Father)',
        'Father Name'
    ],
    'Mother Name': [
        'Mothers Name',
        "Mother's Name",
        "Mother'sName",
        'MothersName',
        'Mother',
        'Mom',
        'Maternal Authority',
        'Maternal',
        'Parent Name (Mother)',
        'Mother Name',
        'MotherName'
    ],
    'Marital Status': [
        'Marital Status',
        'Marriage Status',
        'Relationship Status',
        'Marital Condition'
    ],
    'Spouse Name': [
        'Spouse Name',
        'Spouse',
        'Partner',
        'Spouse Name(If any)',
        'Partner Name',
        'Partners Name',
        "Partner's Name",
        'Husband/Wife Name',
        'Husband Name',
        'Wife Name',
        'Marital Partner',
        'Life Partner'
    ],
    'Educational Qualification': [
        'Educational Qualification',
        'EducationalQualification',
        'Qualification',
        'Ed Qualification',
        'Ed Qualification',
        'Educational Achievement',
        'Achievement',
        'Education Qualification',
        'Education Level',
        'Education',
        'Last Achieved Degree',
        'Achieved Degree',
        'Last Degree',
        'Degree',
        'Educational Background'
    ],
    'National ID': [
        'National Id',
        'ID Number',
        'National ID Number',
        'National ID No',
        'ID No',
        'National Identification Number',
        'National Identification No',
        'National Identity',
        'NID',
        'NID No',
        'NID Number'
    ],
    'Smart ID': [
        'Smart ID',
        'Smart',
        'Smart ID No',
        'Smart Card',
        'Smart Card Number',
        'Smart Card No',
        'Digital ID',
        'Client Smart ID',
        'Customer Smart ID',
        'Individual Smart ID',
        'Smart ID Number',
        'Smart Identification Number',
        'Smart Identification No'
    ],
    'Birth Registration No': [
        'Birth Registration No',
        'Birth Registration Number',
        'Birth ID',
        'Registration ID',
        'Client Birth ID',
        'Customer Registration ID',
        'Birth Identification Number',
        'Birth Identification No'
    ],
    'Other Card Type': [
        'Other Card Type',
        'Card Type',
        'Alternate Card Type',
        'Additional Card',
        'Additional Card Type',
        'Additional Identification',
        'Additional Identification Card'
    ],
    'Card No': [
        'Card No',
        'Card Number',
        'Card Id Number',
        'Card Id No',
        'Other Card No',
        'Other Card Number',
        'Card ID',
        'Other Id',
        'Other Id No',
        'Other Id Number',
        'Identification Number',
        'Client Card Number',
        'Customer Card ID',
        'Card Identification Number',
        'Card Identification No',
        'Member Card Number',
        'Members Card Number',
        "Member's Card Number",
        'Member Card No',
        'Members Card No',
        "Member's Card No",
        'Driving License No',
        'Driving License',
        'Driving License Number'
        'License Number',
        'License No',
        'Passport No',
        'Passport Number',
        'Passport'
    ],
    'Card Issuing Country': [
        'Card Issuing Country',
        'Driving License Issuing Country',
        'Driving Issuing Country',
        'Driving License Issue Country',
        'Driving Issue Country',
        'Passport License Issuing Country',
        'Passport Issuing Country',
        'Passport License Issue Country',
        'Passport Issue Country',
        'Country',
        'Issuing Country',
        'Card Country',
        'Issuing Country',
        'Country of Issue',
        'Country of Card Issuance',
        'Card Issuance Country'
    ],
    'Card Expiry Date': [
        'Card Expiry Date',
        'Expiration Date',
        'Valid Until',
        'Card Validity Date',
        'Expiry Date',
        'Card Expiration Date',
        'Card Valid Date',
        'Passport Valid Date',
        'Driving License Valid Date',
        'License Valid Date',
        'Passport Expiration Date',
        'Driving License Expiration Date',
        'License Expiration Date',
        'Passport Expiry Date',
        'Driving License Expiry Date',
        'License Expiry Date'
    ],
    'Form Application No': [
        'Form Application No',
        'Form No',
        'Form Number',
        'Application No',
        'Application Number',
        'Form ID',
        'Application ID',
        'Client Application Number',
        'Form Identification Number',
        'Form Identification No'
    ],
    'Member Type': [
        'Member Type',
        'Type of Member',
        'User Type',
        'Account Type',
        'Client Type',
        'Customer Type',
        'Individual Type',
        'Members Type',
        "Member's Type",
        'Customers Type',
        "Customer's Type",
        'Clients Type',
        "Client's Type",
        'Users Type',
        "User's Type"
    ],
    'Status': [
        'Status',
        'Member Status',
        'Members Status',
        'User Status',
        'Users Status',
        "User's Status",
        "Member's Status",
        'Current Status',
        'Active Status',
        'Client Status',
        'Customer Status',
        'Individual Status',
    ],
    'Mobile Number': [
        'Mobile No',
        'MobileNo',
        'Mobile Number',
        'MobileNumber',
        'Phone Number',
        'PhoneNumber',
        'PhnNumber',
        'Mbl No',
        'Mbl Number',
        'Contact Number',
        'Cell Phone',
        'Client Phone Number',
        'Phone',
        'Contact No',
        'Contact',
        'Cell Number',
        'Cell No',
        'Cell'
    ],
    'Family Home Contact No': [
        'Family Home Contact No',
        'Home Phone',
        'Family Contact No',
        'Family Contact Number',
        'Permanent Contact No',
        'Permanent Contact Number',
        'Household Contact',
        'Residence Contact',
        'Family Contact',
        'Emergency Contact',
        'Emergency Cell',
        'Emergency Cell Phone'
    ],
    'Pass Book Number': [
        'Passbook No',
        'Psbk No',
        'Psbk Number',
        'Pb No',
        'Passbook',
        'Pass Book',
        'Passbook Number',
        'Member Passbook',
        'Client Passbook',
        'Client Passbook No',
        'Client Passbook Id',
        'Member Passbook No',
        'Member Passbook Number',
        'Member Pass book Number',
        'Member Pass book No',
        'Members Pass book No',
        'Members Passbook No',
        'Members Pass book Number',
        'Members Passbook Number',
        'Members Passbook Id',
        'Members Pass book Id',
        "Member's Pass book No",
        "Member's Passbook No",
        "Member's Pass book Number",
        "Member's Passbook Number",
        "Member's Passbook Id",
        "Member's Pass book Id",
        'Passbook Id',
        'Pass book Id',
        'Pass Book Number',
        'Client Passbook Number'
    ],
    'Passbook Amount': [
        'Passbook Amount',
        'Passbook fee',
        'Client Passbook Amount',
        'Client Passbook fee',
        'Member Passbook Amount',
        'Member Passbook fee',
        'Members Passbook Amount',
        'Members Passbook fee',
        "Member's Passbook Amount",
        "Member's Passbook fee"
    ],

    # Loans Features patterns
    'Disbursement Date': [
        'Date',
        'Disbursement Date',
        "D Date",
        "Disb Date",
        'Disburse Date',
        'Date of Disbursement',
        'Fund Transfer Date',
        'Payment Release Date'
        'Money Transfer Date',
        'Release Date for Funds',
        'Distribution Date',
        'Finance Release Date',
        'Cash Transfer Date',
        'Payment Settlement Date',
        'Payment Date',
        'Remittance Date',
        'Finance Date',
        'Settlement Date',
        'Release Date'
    ],

    'Loans Product': [
        'Product',
        'LP',
        'Ln Pr',
        'Loan Product',
        'Loans Product',
        'Loan Product Id',
        'Loans Product No',
        'Product Id',
        'L Product Id',
        'LPId',
        'LPNo',
        'LP Number',
        'LP Id',
        'LId',
        'LNo',
        'LNumber',
        'LPCode',
        'LP code',
        'lcode',
        'l code',
        'ln code',
        'lnNo',
        'lnId',
        "Loan's code"
        'Loan Solution',
        'Loan Option',
        'Loan Facility',
        'Loan Program',
        'Loan Package',
        'Loan Offering'
        'Financing Product',
        'Financing Solution',
        'Financing Option',
        'Financing Facility',
        'Financing Program',
        'Financing Offering',
        'Borrowing Product',
        'Borrowing Solution',
        'Borrowing Option',
        'Borrowing Facility',
        'Borrowing Program',
        'Borrowing Offering',
        'Credit Product',
        'Credit Solution',
        'Credit Option',
        'Credit Facility',
        'Credit Program',
        'Credit Offering',
        'Lending Product',
        'Lending Solution',
        'Lending Option',
        'Lending Facility',
        'Lending Program',
        'Lending Offering',
        'Financial Offering',
        'Financial Product',
        'Financial Solution',
        'Financial Option',
        'Financial Facility',
        'Financial Program',
        'Funding Offering',
        'Funding Product',
        'Funding Solution',
        'Funding Option',
        'Funding Facility',
        'Funding Program',
        'Mortgage Offering',
        'Mortgage Product',
        'Mortgage Solution',
        'Mortgage Option',
        'Mortgage Facility',
        'Mortgage Program',
        'Debt Instrument',
    ],

    'Loan Code': [
        'Loan No',
        'Loan Number',
        'Customized Loan Number',
        'Customized Loan No',
        'Loan Code',
        'LId',
        'LNo',
        'LNumber',
        'LPCode',
        'LPCode',
        'LP code',
        'lcode',
        'l code',
        'ln code',
        'lnNo',
        'lnId',
        "Loan's code"
        'Loan Registration Code',
        'Loans Registration Code',
        'Loans Code',
        'Loan ID',
        'Loans ID',
        'User Loan ID',
        'Client Loan ID',
        'Customer Loan ID',
        'Individual Loan ID',
        'Loan Identification No',
        'Loan Identification Number'
    ],

    'Repayment Frequency': [
        'Frequency',
        'Repay Freq',
        'Repayment Fr',
        'Rf',
        'Rf count',
        'Repayment Frequency',
        'Repay Frequency',
        'Payment Interval',
        'Installment Frequency',
        'Installment Schedule',
        'Repayment Period',
        'Frequency of Payments',
        'Payment Cycle',
        'Payment Frequency',
        'Recurring Payments',
        'Repayment Schedule',
        'Payment Rhythm',
        'Interval of Reimbursement',
        'Payment Periodicity',
        'Recurrence Rate',
        'Reimbursement Frequency',
        'Refund Frequency',
        'Recurring Payment Rate'
    ],

    'Loan Repay Period': [
        'Loan Repay Period',
        'Loan Repay Period in Month',
        'Loan Repay Period in Day',
        'Loan Repay Period in Days',
        'Loan Period in Month',
        'Loan Period in Day',
        'Loan Period in Days',
        'L repay period',
        'Ln repay period',
        'Lr period',
        'L period',
        'Ln period',
        'Loans Repay Period',
        'Repayment Duration',
        'Loan Payback Time',
        'Refund Period',
        'Payment Term',
        'Repayment Term'
        'Repayment Window',
        'Loan Return Period',
        'Payoff Timeframe',
        'Refund Span',
        'Payment Cycle',
        'Payback Duration',
        'Repayment Timeline',
        'Refund Interval'

    ],

    'First Repay Date': [
        'First Repay Date',
        '1st repay date',
        'first Rp Date',
        'first R date',
        '1st Rp Date',
        '1st R date',
        'First Repayment Date',
        'First Repay',
        'Repay Date',
        'Initial Payment Date',
        'First Payment Due Date',
        'Inaugural Repayment Date',
        'Primary Repayment Date',
        'Initial Reimbursement Date',
        'First Installment Date',
        'Debut Payment Date',
        'Commencement of Repayment Date',
        'Onset of Payment Date',
        'Maiden Repayment Date',
        'Primary Payment Date',
        'Initial Repayment Date',
        'First Settlement Date',
        'Inceptive Payment Date',
        'Genesis Repayment Date',
        'Opening Payment Date',
        'Earliest Repayment Date',
        'Commencement Payment Date',
        'Initial Refund Date'
    ],

    'Loan Cycle': [
        'Cycle',
        'Loan Cycle',
        'Loans Cycle',
        'LCycle',
        'Ln Cycle',
        'LnCycle',
        'LC',
        'lc No',
        'lc Number',
        'lc count',
        'l cycle',
        'Loan Cycle No',
        'Loans Cycle No',
        'Loan Cycle Number',
        'Loans Cycle Number',
        'Financing Period',
        'Borrowing Phase',
        'Credit Cycle',
        'Lending Period',
        'Loan Duration',
        'Repayment Cycle',
        'Borrowing Cycle',
        'Funding Period',
        'Loan Term',
        'Credit Duration',
        'Financing Cycle',
        'Credit Term',
        'Borrowing Cycle',
        'Lending Cycle',
        'Loan Repayment Cycle',
        'Funding Cycle',
        'Credit Period',
        'Debt Cycle',
        'Financing Term',
        'Repayment Term'

    ],

    'Loan Amount': [
        'Amount',
        'loan amt',
        'amt',
        'l amount',
        'ln amount',
        'lp amount',
        'l amt',
        'ln amt',
        'lp amt',
        'Loan amt',
        'LAmount',
        'LnAmount',
        'LpAmount',
        'LAmt',
        'LnAmt',
        'LpAmt',
        'L A',
        'Ln A',
        'Lp A',
        'Loan Amount',
        'Loans Amount',
        'LAmount',
        'Borrowed Sum',
        'Financing Size',
        'Credit Value',
        'Borrowing Quantity',
        'Funded Amount',
        'Loan Size',
        'Finance Value',
        'Borrowed Quantity',
        'Principal Amount',
        'Loan Sum',
        'Borrowed Capital',
        'Financing Amount',
        'Credit Sum',
        'Borrowing Limit',
        'Funded Sum',
        'Loaned Capital',
        'Finance Limit',
        'Borrowed Value',
        'Principal Sum',
        'Loan Capital'
    ],

    'No Of Repayment': [
        'No Of Repayment',
        'No Of Installment',
        'Number Of Installment',
        'Total Installment',
        'Total Installment Number',
        'Lr No',
        'LrNo',
        'LrNumber',
        'RepayNo',
        'RepayNumber',
        'LR number',
        'No of LR',
        'Number of Repayment',
        'Repayment Number',
        'No Of Repayment',
        'Repayment Count',
        'Number of Payments',
        'Installment Quantity',
        'Repayment Quantity',
        'Payment Total',
        'Repayment Volume',
        'Installment Count',
        'Payment Amount',
        'Number of Refunds',
        'Repayment Total'
        'Payment Frequency',
        'Installment Frequency',
        'Repayment Frequency',
        'Payment Count',
        'Installment Count',
        'Refund Quantity',
        'Payment Series',
        'Repayment Series',
        'Refund Frequency',
        'Payment Schedule'

    ],

    'Insurance Amount': [
        'Insurance Amount',
        'Insurance',
        'Ins',
        'Ins Amount',
        'InsAmount',
        'InsAmt',
        'InsuranceAmt',
        'InsuranceAmount',
        'Ins Amt',
        'I Amt',
        'I amount',
        'IA',
        'Amount of Insurance',
        'Coverage Sum',
        'Insured Value',
        'Policy Amount',
        'Protection Amount',
        'Coverage Level',
        'Insurance Value',
        'Insured Sum',
        'Policy Value',
        'Coverage Amount',
        'Insurance Coverage',
        'Assurance Sum',
        'Protection Value',
        'Coverage Quantity',
        'Insured Amount',
        'Policy Sum',
        'Assurance Value',
        'Coverage Value',
        'Protection Amount',
        'Insured Value',
        'Policy Coverage',
        'Indemnity Value',
        'Policy Limit',
        'Coverage Sum',
        'Assured Amount',
        'Compensation Value',
        'Policy Value',
        'Coverage Limit',
        'Insurable Amount',
        'Risk Coverage',
        'Assurance Value'
    ],

    'Loan Purpose': [
        'Loan Purpose',
        'Purpose',
        'Purpose ID',
        'Purpose Name',
        'Loans Purpose',
        'Loan Purpose Name',
        'Loans Purpose Name',
        'Loan Purpose ID',
        'Loans Purpose ID',
        'LP ID',
        'LP',
        'LP Name',
        'L Purpose',
        'Ln Purpose',
        'LPurpose',
        'LnPurpose',
        'Purpose',
        'Purpose of Loan',
        'Borrowing Intent',
        'Financing Objective',
        'Credit Goal',
        'Loan Use',
        'Borrowing Purpose',
        'Funding Purpose',
        'Loan Intent',
        'Finance Goal',
        'Credit Purpose',
        'Borrowing Reason',
        'Borrowing Motive',
        'Financing Purpose',
        'Credit Objective',
        'Loan Objective',
        'Borrowing Aim',
        'Funding Objective',
        'Credit Intent',
        'Loan Goal',
        'Borrowing Target',
        'Finance Purpose',
        'Financing Use',
        'Credit Destination',
        'Loan Mission',
        'Borrowing Need',
        'Borrowing End',
        'Financial Goal'
    ],

    'Folio Number': [
        'Folio Number',
        'Folio No',
        'Ref No',
        'Ref Number',
        'F No',
        'F Number',
        'FNo',
        'FNumber',
        'FId',
        'RefId',
        'RefCode',
        'Account Identifier',
        'Reference Code',
        'Portfolio ID',
        'Investment Identifier',
        'Account Number',
        'Folio ID',
        'Portfolio Number',
        'Transaction Code',
        'Asset ID',
        'Account Code',
        'Record Number',
        'Ledger Number',
        'Document Identifier',
        'Serial Number',
        'Reference Number',
        'Identification Number',
        'Book Number',
        'File Number',
        'Registry Number',
        'Index Number'
    ],

    'Interest Discount Amount': [
        'Interest Discount',
        'Interest Discount Amount',
        'Discount Amount',
        'Interest Deduction',
        'Discounted Interest',
        'Interest Reduction',
        'Deducted Interest',
        'Interest Rebate',
        'Discounted Finance Charge',
        'Reduced Interest Amount',
        'Interest Deduction Value',
        'Discounted Interest Cost',
        'Reduced Finance Charge',
        'Interest Rebate',
        'Interest Adjustment',
        'Discounted Interest Value',
        'Interest Concession',
        'Interest Allowance',
        'Interest Deduction',
        'Discounted Interest Sum',
        'Interest Subsidy',
        'Interest Offset',
        'Discounted Interest Charge'
    ],

    'Installment Amount': [
        'Installment Amount',
        'Installment',
        'Installment Amt',
        'InstallmentAmt',
        'IAmt',
        'IAmount',
        'I Amount',
        'Amount of Installment',
        'Payment Sum',
        'Repayment Value',
        'Scheduled Payment',
        'Regular Payment',
        'Installment Value',
        'Repayment Amount',
        'Payment Amount',
        'Scheduled Amount',
        'Regular Installment',
        'Payment Value',
        'Payment Sum',
        'Repayment Value',
        'Scheduled Payment'
        'Regular Payment',
        'Repayment Amount',
        'Payment Amount',
        'Scheduled Amount',
        'Regular Installment',
        'Repayment Sum',
        'Payment Total'

    ],

    'Opening Loan Outstanding': [
        'Opening Loan Outstanding',
        'Opening Loan Outstanding Amount',
        'Opening Loan Outstanding Amt',
        'Opn Ln Outstanding',
        'Opn Ln Outstanding Amount',
        'Opn L Outstanding',
        'OpnLnOutstanding',
        'OpnLOutstanding',
        'LnOutstanding',
        'LOutstanding',
        'LpOutstanding',
        'OpnLpOutstanding',
        'Opening Outstanding',
        'Initial Loan Balance',
        'Beginning Loan Amount',
        'Opening Loan Balance',
        'Initial Outstanding Balance',
        'Starting Loan Amount',
        'Opening Loan Amount',
        'Initial Loan Outstanding',
        'Commencement Loan Balance',
        'Inception Loan Outstanding',
        'Initial Debt Balance',
        'Starting Debt Balance',
        'Initial Loan Arrears',
        'Beginning Loan Debt',
        'Inceptive Loan Balance',
        'Opening Debt Amount',
        'Commencement Loan Arrears',
        'Genesis Loan Outstanding',
        'Initial Loan Liability',
        'Inaugural Loan Debt',
        'Starting Loan Arrears'

    ],

    'Extra Installment Amount': [
        'Extra Installment Amount',
        'Advance Installment Amount',
        'Extra Amount',
        'Advance Amount',
        'Additional Payment Sum',
        'Supplementary Repayment',
        'Extra Payment Value',
        'Additional Installment',
        'Supplemental Repayment',
        'Bonus Payment Amount',
        'Surplus Installment',
        'Extra Repayment Value',
        'Bonus Installment',
        'Additional Contribution',
        'Additional Payment',
        'Excess Installment',
        'Supplementary Payment',
        'Overpayment Amount',
        'Bonus Installment',
        'Surplus Payment',
        'Incremental Installment',
        'Added Payment',
        'Top-Up Installment',
        'Overdue Installment'
    ],

    'Guarantor Name': [
        'Name',
        'GName',
        'Guarantor Name',
        'GuarantorName',
        "Guarantor's Name",
        'Guarantor',
        "Guarantor's Name",
        "Guarantor's Full Name",
        'Guarantors Name',
        'Guarantors Name',
        "Client's Guarantor Name",
        "Customer's Guarantor Name",
        "Individual's Guarantor Name",
        "Member's Guarantor Full Name",
        "Person's Guarantor Full Name",
        "Member's Guarantor First Name",
        "Member's Guarantor Last Name",
        "Client's Guarantor Full Name",
        "Customer's Guarantor Full Name",
        "Individual's Guarantor Full Name"

    ],

    'Relation': [
        'Relation',
        'Relation with Guarantor'
        'Connection',
        'Connection with Guarantor',
        'Guarantor Relation',
        'Guarantor Relationship',
        'Association',
        'Link',
        'Affiliation',
        'Bond',
        'Relationship',
        'Tie',
        'Interaction',
        'Correspondence',
        'Involvement',
        'Kinship',
        'Affinity',
        'Alliance',
        'Rapport',
        'Attachment',
        'Association',
        'Proximity',
        'Interconnection',
        'Relevance',
        'Nexus'
    ],

    'Address': [
        'Address',
        'Addr',
        'Address of Guarantor',
        "Guarantor's Address",
        'Location',
        'Location of Guarantor',
        'Residence',
        'Residence of Guarantor'
        'Place',
        'Place oF Guarantor',
        'Dwelling',
        'Dwelling of Guarantor'
        'Abode',
        'Abode of Guarantor',
        'Domicile',
        'Domicile of Guarantor',
        'Street',
        'Street of Guarantor',
        'Habitat',
        'Habitat of Guarantor',
        'Site',
        'Site of Guarantor',
        'Position',
        'Position of Guarantor',
        'Street',
        'Street of Guarantor',
        'Home',
        'Home of Guarantor'
    ],

    'Guarantors Contact': [
        'Contact No of Guarantor',
        'Guarantors Contact No',
        "Guarantor's Contact No",
        'Mobile No',
        'Mobile Number',
        'Phone Number',
        'Contact Number',
        'Cell Phone',
        'Client Phone Number',
        'Phone',
        'Contact No',
        'Contact',
        'Cell Number',
        'Cell No',
        'Cell',
        'MobNo',
        'Mob Number',
        'Phn Number',
        'Phn No',
        'PhnNumber',
        'PhnNo'
    ],
    # Savings features mapping

    "Savings Code": [
        "Saving",
        "Savings",
        "Code",
        "Customised Savings Name",
        "Customized Savings Name",
        "Customised Saving Name",
        "Customized Saving Name",
        "Customised Saving Id",
        "Customized Saving Id",
        "Customised Savings Id",
        "Customized Savings Id",
        "CustomisedSavingsId",
        "CustomizedSavingsId",
        "CustomisedSavingId",
        "CustomizedSavingId",
        "CustomisedSavingsName",
        "CustomizedSavingsName",
        "CustomisedSavingName",
        "CustomizedSavingName",
        "Saving's",
        "Savings Code",
        "Saving's Code",
        "Savings ID",
        "SavingsID",
        "SavingID",
        "Saving ID",
        "sav code",
        "sv code",
        "SavingCode",
        "SavingsCode",
        "SavCode",
        "SvCode",
        "SavID",
        "SvID",
        "Sav Name",
        "SavName",
        "Saving Name",
        "SavingName",
        "SvName",
        "SName",
        "S Name",
        "SCode",
        "S Code",
        "Saving Product Name",
        "Savings Product Name",
        "SavingProductName",
        "Savings Product ID",
        "SavingsProductID",
        "SProdName",
        "SProdCode",
        "SProdId",
        "SID",
        "S Id",
    ],

    "Savings Product": [
        "Name of product",
        "Product Name",
        "PrID",
        "P Id",
        "PId",
        "PName",
        "P Name",
        "P",
        "Prdct",
        "Prdct Name",
        "Prdct Id",
        "PrdctName",
        "PrdctId",
        "ProductID",
        "ProductIdentificationNo",
        "Product Identification No",
        "Pr Name",
        "PrName",
        "Pr",
        "product",
        "product's Name",
        "products Name",
        "Products",
        "Product's Name",
        "Brand",
        "Brand title",
        "Brand's",
        "Brands",
        "brand",
        "brand's",
        "Item label",
        "Items",
        "item",
        "items",
        "Product designation",
        "Product designation",
        "Product_designation",
        "Trade name",
        "Trade's name",
        "Trades name",
        "Product identifier",
        "Branded product",
        "Branded_product",
        "Brandeds product",
        "Branded's product",
        "Named commodity",
        "Commodity denomination",
        "Labeling",
        "Trade designation",
        "Marked item",
        "Savings product name",
        "SavingsProductName",
        "Saving product name",
        "SavingProductName",
        "SProdName",
        "SName",
        "S Prod Name",
        "SavProdName",
        "SvProdName",
        "Savings Product",
        "Saving Product ID",
        "Sav Prod Name",
        "Sv Prod Name",
        "SP Name",
        "S P Name",
        "SName",
        "S Name",
        "Sp",
        "S",
    ],

    "Saving Cycle": [
        "Savings cycle",
        "SavingsCycle",
        "Cycle",
        "Cyc",
        "Saving Cycle",
        "SavingCycle",
        "SCycle",
        "Sv Cycle",
        "SavCycle",
        "SC",
        "Sc No",
        "Sc Number",
        "Sc count",
        "s cycle",
        "Saving Cycle No",
        "Savings Cycle No",
        "S Cycle No",
        "SP Cycle Number",
        "Savings Cycle Number",
        "Saving Cycle Number",
        "Sav Cycle Number",
        "Sv Cycle Number",
        "S Cycle Number",
        "Saving Period",
        "SavingPeriod",
        "SavingsPeriod",
        "Savings Period",
        "Accumulation Phase",
        "Investment Cycle",
        "Deposit Cycle",
        "Saving Term",
        "Savings Term",
        "SavingTerm",
        "SavingsTerm",
        "Saver's Period",
        "Reserve Cycle",
        "Hoarding Phase",
        "Conservation Period",
        "Stashing Cycle",
    ],

    "Savings Opening Balance": [
        "Opening Deposit Amount",
        "Opening Deposit Amt",
        "Opening Deposit",
        "Op Balance",
        "OpBalance",
        "OpeningBalance",
        "Opening Balance",
        "Open Balance",
        "OpenBalance",
        "Bal",
        "OB",
        "OpB",
        "OpBal",
        "Op Bal",
        "OB",
        "O B",
        "Open Balance",
        "OpenBalance",
        "Open Blnc",
        "OpenBlnc",
        "acc opening balances",
        "Account Opening Balance",
        "AccountOpeningBalance",
        "Balance",
        "Initial Balance",
        "Starting Balance",
        "Beginning Balance",
        "Introductory Balance",
        "Launch Balance",
        "Genesis Balance",
        "Kickoff Balance",
        "Onset Balance",
        "Commencement Balance",
        "Primary Balance",
    ],

    "Auto Process/Monthly Deposit Amount": [
        "Auto Process Amount",
        "AutoProcessAmount",
        "Auto Process Amounts",
        "AutoProcessAmount",
        "auto process amounts",
        "Pr Amt",
        "Auto process",
        "AutoProcess",
        "Auto Process Amt",
        "Auto Process Am",
        "Auto Pr Am",
        "A P Am",
        "A P A",
        "APA",
        "Auto Pr Amt",
        "AutoPrAmt",
        "Process Amt",
        "Process Amount",
        "Weekly Savings",
        "Weekly Saving",
        "Monthly Saving",
        "Monthly Savings",
        "Automated Transaction Sum",
        "Self-executing Payment Value",
        "Programmed Processing Figure",
        "Automatic Transaction Amount",
        "System-Generated Transaction Total",
        "Self-Processing Sum",
        "Machine-Processed Value",
        "Auto-Execution Sum",
        "Algorithmically Processed Amount",
        "Automated Payment Total",

        "MDP",
        "Deposit Amount in Month",
        "Monthly Deposit Amount",
        "MonthlyDepositAmount",
        "MonthlyDepAmount",
        "MonthlyDepAmt",
        "PeriodicDepositAmount",
        "Periodic Dep Amount",
        "Periodic Deposit Amount",
        "Monthly deposit amount",
        "Monthly contribution",
        "Regular deposit sum",
        "Regular deposits sum",
        "Regular deposit's sum",
        "Recurring payment figure",
        "Monthly installment",
        "Monthly installments",
        "Monthly installment's",
        "Periodic deposit amount",
        "Periodic deposit amounts",
        "Monthly savings deposit",
        "Monthly saving's deposit",
        "Monthly Saving Deposit",
        "Consistent monthly contribution",
        "Monthly funding quantity",
        "Routine deposit value",
        "Scheduled monthly amount",
    ],

    "Savings Opening Date": [
        "Date",
        "Op Date",
        "Open Date",
        "OpenDate",
        "OpeningDate",
        "OpDt",
        "Op Dt",
        "Opening Date",
        "Start Date",
        "Commencement Date",
        "Inauguration Date",
        "Genesis Date",
        "Initiation Date",
        "Launch Date",
        "Onset Date",
        "Kickoff Date",
        "Beginning Date",
        "Introduction Date",
    ],

    'Period': [
        "period",
        "Saving Period in Days",
        "Savings Period in Days",
        "Saving Period in Month",
        "Savings Period in Month",
        "P",
        "periods",
        "period's",
        "Period",
        "Duration",
        "Interval",
        "Timeframe",
        "Term",
        "Phase",
        "Epoch",
        "Spell",
        "Cycle",
    ],

    "Mature Date": [
        "Mature Date",
        "MatureDate",
        "MDate",
        "Mat Date",
        "MatDate",
        "Maturity",
        "MD",
        "DueDate",
        "Expiry date",
        "Deadline",
        "End date",
        "Completion date",
        "Final date",
        "Termination date",
        "Closing date",
        "Due date",
        "Conclusion date",
        "Fulfillment date",
    ],

    "Payable Amount": [
        "Payable Amount",
        "Pay Amount",
        "PayAmount",
        "Pay Amt",
        "P Amount",
        "PAmount",
        "PayAmt",
        "PayBalance",
        "PAmt",
        "PA",
        "PayableAmount",
        "Outstanding balance",
        "Amount due",
        "Sum payable",
        "Payable balance",
        "Owed amount",
        "Outstanding payment",
        "Unpaid balance",
        "Receivable sum",
        "Amount to be paid",
        "Balance due",
    ],

    "Interest Calculation Period": [
        "Interest Calculation",
        "Int Calculation",
        "Interest Cal",
        "Interest Calculation Period",
        "Int Calculation Period",
        "Interest Cal Period",
        "Interest Calculation Time Period"
    ],

    "Mode Of Payment": [
        "Mode Of Payment",
        "Mode",
        "Mode Of Payments",
        "ModeOfPayment",
        "MOfPayment",
        "Payment's Mode",
        "Payment method",
        "Payment methods",
        "Payment option",
        "Payment options",
        "Payment mode",
        "Payment modes",
        "Payment's mode",
        "Form of payment",
        "Payment's form",
        "Transaction medium",
        "Transaction's Medium",
        "Remittance mode",
        "Remittance Mode",
        "Payment channel",
        "Payment Channel",
        "Means of payment",
        "Means of Payment",
        "Means of payment's",
        "Financial instrument",
        "Financial Instrument",
        "Settlement method",
    ],
    "Nominee Name": [
        "Name",
        "Nominee's Name",
        "Nominee",
        "Nominee's Name",
        "Nominee's Full Name",
        "Nominee Name",
        "Nominees Name",
        "Client's Nominee Name",
        "Customer's Nominee Name",
        "Individual's Nominee Name",
        "Member's Nominee Full Name",
        "Person's Nominee Full Name",
        "Member's Nominee First Name",
        "Member's Nominee Last Name",
        "Client's Nominee Full Name",
        "Customer's Nominee Full Name",
        "Individual's Nominee Full Name",
    ],

    "Nominee Relation": [
        "Nominee Relation",
        "Nominee Relationship",
        "Nominee's Relation",
        "Relation",
        "Relation with Nominee",
        "Nominee Relation",
        "Rltn",
        "Rltn with Nominee",
        "R",
        "Relations",
        "Relation's",
        "relation",
        "relation's",
        "Relationship",
        "Connection",
        "Association",
        "Bond",
        "Affiliation",
        "Tie",
        "Link",
        "Affinity",
        "Interrelation",
        "Kinship",
        "Nominee Relations",
        "Relations with Nominee",
        "Relationship with Nominee",
        "Nominee Relationship",
        "Relationship",
        "Nominee Relations",
        "Nominee Connection",
        "Connection with Nominee",
        "Association",
        "Nominee Association",
        "Association with Nominee",
        "Nominee Bond",
        "Bond with Nominee",
        "Nominee Affiliation",
        "Affiliation with Nominee",
        "Tie",
        "Link",
        "Affinity",
        "Interrelation",
        "Kinship",
    ],

    "Nominee Share": [
        "Share",
        "Nominee Share",
        "Nominees Share",
        "Shr",
        "Share",
        "Part",
        "Nominee Part",
        "Nominee's Part",
        "Sh",
        "Portion",
        "Portions",
        "Nominee Portion",
        "Nominee's Portion",
        "Nominee Portions",
        "Nominee's Portions",
        "Nominee Portions",
        "NomineePart",
        "NomineesPart",
        "Part's",
        "Allocation",
        "Division",
        "Quota",
        "Segment",
        "Percentage",
        "Fraction",
        "Fr",
        "Fractions",
        "Allotment",
        "Contribution",
        "Contributions",
    ]
}

"""# **Feature Extraction**"""


def preprocess_feature_name(name):
    # Convert name to lowercase and remove special characters
    processed_name = re.sub(r'[^a-zA-Z0-9\s]', '', name.lower())
    return processed_name.strip()


def preprocess_column_names(columns):
    # Preprocess all column names in the dataset
    processed_columns = []
    for col in columns:
        # Replace special characters with spaces and convert to lowercase
        processed_col = re.sub(r'[^a-zA-Z0-9]', ' ', col.lower())
        processed_columns.append(processed_col.strip())
    return processed_columns


def format_feature_values(df):
    for col in df.columns:
        if 'date' in col.lower():
            try:
                df[col] = pd.to_datetime(df[col], errors='coerce').dt.strftime('%Y-%m-%d')
            except Exception as e:
                print(f"Error processing column {col}: {e}")
        elif pd.api.types.is_numeric_dtype(df[col]):
            df[col] = df[col].apply(
                lambda x: f'{int(x)}' if pd.notnull(x) and x == int(x) else f'{x:.2f}' if pd.notnull(x) else x)
    return df


def extract_desired_features(dataset_path, desired_features_fuzzy, threshold=90):
    # Detect file extension
    file_extension = Path(dataset_path).suffix.lower()

    if file_extension == '.xls':
        # Load Excel .xls file
        dataset = pd.read_excel(dataset_path, engine='xlrd')
    elif file_extension == '.xlsx':
        # Load Excel .xlsx file
        dataset = pd.read_excel(dataset_path, engine='openpyxl')
    elif file_extension == '.csv':
        # Load CSV file
        dataset = pd.read_csv(dataset_path)
    else:
        raise ValueError(f"Unsupported file format: {file_extension}. Only .xls, .xlsx, or .csv files are supported.")
    # Preprocess dataset column names
    dataset.columns = preprocess_column_names(dataset.columns)

    # Initialize DataFrame to store selected features
    selected_features = pd.DataFrame()

    # Iterate over each desired feature
    for feature_name, possible_names in desired_features_fuzzy.items():
        # Preprocess feature name
        processed_feature_name = preprocess_feature_name(feature_name)

        # Try to find the best match in the dataset
        best_match = None
        best_score = 0
        for col in dataset.columns:
            # Calculate the similarity score using fuzzy string matching
            score = fuzz.token_sort_ratio(processed_feature_name, col)
            if score > best_score:
                best_match = col
                best_score = score

        # Check if the best match meets the similarity threshold
        if best_score >= threshold:
            selected_features[feature_name] = dataset[best_match]
        else:
            # If no good match found, try alternative names
            for alt_name in possible_names:
                processed_alt_name = preprocess_feature_name(alt_name)
                alt_match = None
                alt_score = 0
                for col in dataset.columns:
                    score = fuzz.token_sort_ratio(processed_alt_name, col)
                    if score > alt_score:
                        alt_match = col
                        alt_score = score
                if alt_score >= threshold:
                    selected_features[feature_name] = dataset[alt_match]
                    break  # Use the first acceptable alternative match

    # Drop rows where all values are NaN (null)
    selected_features.dropna(axis=0, how='all', inplace=True)

    format_feature_values(selected_features)

    return selected_features


"""# **Member Migration Screen**"""


def process_member_migration(df):
    # Member migration all the features
    member_migration = [
        'Name', 'Member Surname', 'Admission Date', 'Primary Product', 'Samity Name',
        'Samity Code', 'Age', 'Date Of Birth', 'Member Code', 'Village/Block Ward',
        'Post Office', 'Gender', 'Father Name', 'Mother Name', 'Marital Status',
        'Spouse Name', 'Educational Qualification', 'National ID', 'Smart ID',
        'Birth Registration No', 'Other Card Type', 'Card No', 'Card Issuing Country',
        'Card Expiry Date', 'Form Application No', 'Member Type', 'Status',
        'Mobile Number', 'Land Area', 'Family Home Contact No', 'Pass Book Number',
        'Passbook Amount'
    ]
    # Create a new DataFrame with the desired sequence of columns
    new_df = pd.DataFrame(columns=member_migration)

    # Merge existing data from df into the new DataFrame based on the sequence of columns
    for col in member_migration:
        if col in df.columns:
            new_df[col] = df[col]
        else:
            new_df[col] = np.nan

    columns_to_check = ['National ID', 'Smart ID', 'Birth Registration No', 'Other Card Type']
    new_df[columns_to_check] = new_df[columns_to_check].astype(float)
    new_df[columns_to_check] = new_df[columns_to_check].replace(0, np.nan)

    # Define mandatory columns for Member Migration
    mandatory_columns = [
        'Name', 'Admission Date', 'Primary Product', 'Date Of Birth', 'Member Code',
        'Village/Block Ward', 'Gender', 'Father Name', 'Mother Name', 'Marital Status',
        'Mobile Number'
    ]

    # Apply condition checks
    valid_rows = (
            new_df[mandatory_columns].notnull().all(axis=1) &
            (~new_df['Marital Status'].isin(['Married', 'Widow', 'Widower', 'M', 'W']) | new_df[
                'Spouse Name'].notnull()) &
            ((new_df[['National ID', 'Smart ID', 'Birth Registration No', 'Other Card Type']].notnull().sum(
                axis=1) > 0) |
             (new_df['Other Card Type'].notnull() & new_df[
                 ['Card No', 'Card Issuing Country', 'Card Expiry Date']].notnull().all(axis=1))) &
            (new_df['Samity Name'].notnull() | new_df['Samity Code'].notnull())
    )

    # Identify ignored rows with reasons
    ignored_rows = new_df[~valid_rows].copy()
    ignored_rows['Missing Columns'] = ''

    # Specify reasons based on conditions
    for index, row in ignored_rows.iterrows():
        reason = []
        if row[mandatory_columns].isnull().any():
            missing_cols = [col for col in mandatory_columns if pd.isnull(row[col])]
            reason.append(', '.join(missing_cols))
        if row['Marital Status'] in ['Married', 'Widow', 'Widower', 'M', 'W'] and pd.isnull(row['Spouse Name']):
            reason.append('Spouse Name')
        if row[['National ID', 'Smart ID', 'Birth Registration No', 'Other Card Type']].isnull().all():
            reason.append('ID/Card details')
        if pd.notnull(row['Other Card Type']) and row[
            ['Card No', 'Card Issuing Country', 'Card Expiry Date']].isnull().any():
            reason.append('Incomplete Card details')
        if pd.isnull(row['Samity Name']) and pd.isnull(row['Samity Code']):
            reason.append('Samity Name and Samity Code')
        ignored_rows.at[index, 'Missing Columns'] = '; '.join(reason)

    # return new_df[valid_rows], ignored_rows
    cleaned_df = new_df[valid_rows]

    cleaned_file_name = "Cleaned Member Data.xlsx"
    ignored_file_name = "Ignore Member Data.xlsx"

    ignore_path = ".\\dataset\\processed\\Ignored"
    cleaned_path = ".\\dataset\\processed\\cleaned"

    ignored_file_path = os.path.join(ignore_path, ignored_file_name)
    cleaned_file_path = os.path.join(cleaned_path, cleaned_file_name)

    cleaned_df.to_excel(cleaned_file_path, index=False)
    ignored_rows.to_excel(ignored_file_path, index=False)

    print(cleaned_file_path)
    return cleaned_file_path


"""# **Loans Migration Screen**"""


def process_loans_migration(df):
    df2 = pd.read_excel(system_generated_member_code)
    # Ensure 'Member Code' columns are of the same type in both DataFrames
    df['Member Code'] = df['Member Code'].astype(str)
    df2['Member Code'] = df2['Member Code'].astype(str)

    merged_df = pd.merge(df, df2, on='Member Code', how='left')
    # Loans migration all the features
    loans_migration = ['Samity Code', 'System Generated Samity Information', 'Member Code',
                       'System Generated Member Information', 'Loans Product', 'Disbursement Date',
                       'Loan Code', 'Repayment Frequency', 'Loan Repay Period', 'First Repay Date', 'Loan Cycle',
                       'Loan Amount', 'No Of Repayment', 'Insurance Amount', 'Loan Purpose', 'Folio Number',
                       'Interest Discount Amount', 'Installment Amount', 'Opening Loan Outstanding',
                       'Extra Installment Amount', 'Guarantor Name', 'Relation', 'Address', 'Contact'
                       ]
    new_df = merged_df.reindex(columns=loans_migration)
    # Merge existing data from df into the new DataFrame based on the sequence of columns
    for col in loans_migration:
        if col in new_df.columns:
            new_df[col] = new_df[col]
        else:
            new_df[col] = np.nan

    # Define mandatory columns for Loans Migration
    mandatory_columns = [
        'System Generated Samity Information', 'Member Code', 'System Generated Member Information', 'Loans Product',
        'Disbursement Date', 'Repayment Frequency', 'Loan Amount', 'No Of Repayment', 'Loan Purpose',
        'Installment Amount', 'Opening Loan Outstanding'
    ]

    # Apply condition checks
    valid_rows = (
        new_df[mandatory_columns].notnull().all(axis=1)
    )

    # Identify ignored rows with reasons
    ignored_rows = new_df[~valid_rows].copy()
    ignored_rows['Missing Columns'] = ''

    # Specify reasons based on conditions
    for index, row in ignored_rows.iterrows():
        reason = []
        if row[mandatory_columns].isnull().any():
            missing_cols = [col for col in mandatory_columns if pd.isnull(row[col])]
            reason.append(', '.join(missing_cols))
        ignored_rows.at[index, 'Missing Columns'] = '; '.join(reason)

    cleaned_df = new_df[valid_rows]

    cleaned_file_name = "Cleaned Loans Data.xlsx"
    ignored_file_name = "Ignore Loans Data.xlsx"

    ignore_path = ".\\dataset\\processed\\Ignored"
    cleaned_path = ".\\dataset\\processed\\cleaned"

    ignored_file_path = os.path.join(ignore_path, ignored_file_name)
    cleaned_file_path = os.path.join(cleaned_path, cleaned_file_name)

    cleaned_df.to_excel(cleaned_file_path, index=False)
    ignored_rows.to_excel(ignored_file_path, index=False)

    print(cleaned_file_path)
    return cleaned_file_path


"""# **Savings Migration Screen**"""


def map_savings_type(product_name):
    if re.search('Astha', product_name, re.IGNORECASE):
        return 'FDR'

    savings_types = {
        'GS': ['GS', 'General Savings', 'Compulsory Savings', 'CS', 'General Saving', 'Gen Savings',
               'Gen Saving', 'GeneralSavings', 'GenSavings', 'GeneralSaving', 'GenSaving',
               'Gen Sav', 'GenSav', 'Regular Savings', 'Regular Saving', 'RegularSavings', 'RegularSaving',
               'Mandatory Savings', 'MandatorySavings', 'Mandatory Saving', 'MandatorySaving', 'General',
               'Mandatory', 'G Saving', 'G Savings', 'M Savings', 'M Saving', 'Mandatory Deposit', 'MandatoryDeposit',
               'General Savings Deposit', 'General Saving Deposit', 'Gen Savings Deposit', 'Gen Saving Deposit',
               'GeneralSavingsDeposit', 'GeneralSavingDeposit', 'GenSavingsDeposit', 'GenSavingDeposit',
               'GeneralSavingsDep', 'GeneralSavingDep', 'GenSavingsDeposit', 'GenSavingDeposit'
               ],

        'VS': ['VS', 'Voluntary Saving', 'Voluntary Savings', 'VoluntarySaving', 'VoluntarySavings', 'V S',
               'Vol Saving', 'Vol Savings', 'VolSaving', 'VolSavings', 'V Saving', 'V Savings', 'VSaving',
               'VSavings', 'Voluntary', 'VSaving', 'VSavings', 'Optional Savings', 'OP', 'Optional Saving'
               ],
        'DPS': ['DPS', 'DPS Savings', 'TSS'],
        'FDR': ['FDR', 'FDR Savings', 'Astha']
    }

    for key, patterns in savings_types.items():
        for pattern in patterns:
            if re.search(pattern, product_name, re.IGNORECASE):
                return key
    return None


def process_savings_migration(df):
    # Read in the system generated member code data
    df2 = pd.read_excel(system_generated_member_code)
    df['Member Code'] = df['Member Code'].astype(str)
    df2['Member Code'] = df2['Member Code'].astype(str)
    merged_df = pd.merge(df, df2, on='Member Code', how='left')

    # Ensure 'Savings Product' column is a string
    merged_df['Savings Product'] = merged_df['Savings Product'].astype(str).fillna('')

    # Savings migration all the features
    savings_migration = [
        'Samity Code', 'Member Code', 'System Generated Samity Information', 'System Generated Member Information',
        'Savings Product', 'Savings Code', 'Saving Cycle', 'Savings Opening Balance',
        'Savings Opening Date', 'Auto Process/Monthly Deposit Amount', 'Period',
        'Mature Date', 'Interest Calculation Period', 'Payable Amount', 'Payment Type',
        'Interest Amount Deposited', 'Interest Amount Paid', 'Nominee Name', 'Relation', 'Share'
    ]

    # Create a new DataFrame and copy the data from merged_df
    new_df = pd.DataFrame(columns=savings_migration)
    for col in savings_migration:
        if col in merged_df.columns:
            new_df[col] = merged_df[col]
        else:
            new_df[col] = np.nan

    # Apply the mapping function to determine savings type for each row
    new_df['Savings Type'] = merged_df['Savings Product'].apply(map_savings_type)

    # Define mandatory columns for Savings Migration
    mandatory_fields = {
        'all': ['Samity Code', 'Member Code', 'System Generated Samity Information',
                'System Generated Member Information', 'Savings Product', 'Savings Opening Date'],
        'non_GS_VS': ['Period', 'Auto Process/Monthly Deposit Amount', 'Payable Amount']
    }

    # Apply condition checks for each row
    valid_rows = []
    ignored_rows = []

    for index, row in new_df.iterrows():
        savings_type = row['Savings Type']
        valid = True
        missing_columns = []

        if savings_type:
            # Check 'all' mandatory fields
            if not all(pd.notnull(row[field]) for field in mandatory_fields['all']):
                valid = False
                missing_columns += [field for field in mandatory_fields['all'] if pd.isnull(row[field])]

            # Additional checks based on savings type
            if savings_type not in ['GS', 'VS']:
                if not all(pd.notnull(row[field]) for field in mandatory_fields['non_GS_VS']):
                    valid = False
                    missing_columns += [field for field in mandatory_fields['non_GS_VS'] if pd.isnull(row[field])]

            if savings_type in ['GS', 'VS', 'DPS']:
                if pd.isnull(row['Savings Opening Balance']):
                    valid = False
                    missing_columns.append('Savings Opening Balance')

            # if savings_type == 'FDR':
            #     if pd.isnull(row['Interest Calculation Period']):
            #         valid = False
            #         missing_columns.append('Interest Calculation Period')

        else:
            valid = False
            missing_columns.append('Savings Type not recognized')

        if valid:
            valid_rows.append(row.drop(labels='Savings Type'))
        else:
            row['Missing Columns'] = ', '.join(missing_columns)
            ignored_rows.append(row.drop(labels='Savings Type'))

    # Convert lists of rows to DataFrames
    valid_df = pd.DataFrame(valid_rows, columns=new_df.columns.drop(['Savings Type']))
    ignored_df = pd.DataFrame(ignored_rows, columns=new_df.columns.tolist() + ['Missing Columns']).drop(
        columns=['Savings Type'])

    # return valid_df, ignored_df

    cleaned_file_name = "Cleaned Savings Data.xlsx"
    ignored_file_name = "Ignore Savings Data.xlsx"

    ignore_path = ".\\dataset\\processed\\Ignored"
    cleaned_path = ".\\dataset\\processed\\cleaned"

    ignored_file_path = os.path.join(ignore_path, ignored_file_name)
    cleaned_file_path = os.path.join(cleaned_path, cleaned_file_name)

    valid_df.to_excel(cleaned_file_path, index=False)
    ignored_df.to_excel(ignored_file_path, index=False)

    print(cleaned_file_path)
    return cleaned_file_path


if __name__ == "__main__":
    dataset_path = sys.argv[1]
    user_input = sys.argv[2]
    system_generated_member_code = ".\\dataset\\Migrated Information\\Migrated Member.xlsx"

    # dataset_path = "C:\\Users\\hp\\Downloads\\SavingsData.xls"
    # user_input = "Savings Migration"
    user_input = user_input.lower()

    # Extract desired features from the user's dataset
    selected_features = extract_desired_features(dataset_path, desired_features_fuzzy)

    # Perform migration based on the migration type
    if user_input == "member migration":
        cleaned_file_path = process_member_migration(selected_features)
    elif user_input == "loans migration":
        cleaned_file_path = process_loans_migration(selected_features)
    elif user_input == "savings migration":
        cleaned_file_path = process_savings_migration(selected_features)
    else:
        print("Unsupported migration type:", user_input)

    # results_df = selected_features.copy()
    #
    # excel_filename = 'feature_extraction_results.xlsx'
    #
    # results_df.to_excel(excel_filename, index=False)
    #
    # print(f"Results saved to {excel_filename}")

    # if user_input == "member migration":
    #     process_member_migration(selected_features)
    # elif user_input == "loans migration":
    #     process_loans_migration(selected_features)

    # Save cleaned and ignored data to Excel files
    # cleaned_file_name = f"{dataset_path.split('.')[0]}_cleaned_data_{datetime.now().strftime('%Y-%m-%d %H-%M-%S')}.xlsx"
    # ignored_file_name = f"{dataset_path.split('.')[0]}_ignored_rows_{datetime.now().strftime('%Y-%m-%d %H-%M-%S')}.xlsx"
    # cleaned_file_name = "Cleaned.xlsx"
    # ignored_file_name = "Ignore.xlsx"
    #
    # ignore_data_path = "C:\\Users\\hp\\Downloads\\Migration\\DataShift\\dataset\\processed\\Ignored\\"
    # cleaned_data_path = "C:\\Users\\hp\\Downloads\\Migration\\DataShift\\dataset\\processed\\cleaned\\"
    # ignored_file_path = os.path.join(ignore_data_path, ignored_file_name)
    # cleaned_file_path = os.path.join(cleaned_data_path, cleaned_file_name)
    #
    # cleaned_df.to_excel(cleaned_file_path, index=False)
    # ignored_df.to_excel(ignored_file_path, index=False)

    # total_cleaned_rows = len(cleaned_df)
    # total_ignored_rows = len(ignored_df)
    # total_process_rows = total_cleaned_rows + total_ignored_rows
    #
    # cleaned_percentage = (total_cleaned_rows / total_process_rows) * 100
    # ignored_percentage = (total_ignored_rows / total_process_rows) * 100
    # print(f"Percentage of cleaned rows: {cleaned_percentage:.2f}%")
    # print(f"Percentage of ignored
