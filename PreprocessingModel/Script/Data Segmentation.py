import os
import sys
from pathlib import Path
import pandas as pd


def get_branch_code_column(dataset):
    possible_name = [
        'Branch Code', 'Branch ID', 'BranchCode', 'BranchID', 'BCode', 'BID',
        'Branch Code No', 'Branch Code Number', 'Branch ID No', 'Branch ID Number',
        'Branch No', 'Branch Number', 'Branch'
    ]
    for col in dataset.columns:
        if col in possible_name:
            return col
    raise ValueError("Branch Code column not found.")


def segment_dataset(dataset_path):
    file_extension = Path(dataset_path).suffix.lower()
    base_file_name = Path(dataset_path).stem

    try:
        if file_extension == '.xls':
            dataset = pd.read_excel(dataset_path, engine='xlrd', dtype=str)
        elif file_extension == '.xlsx':
            dataset = pd.read_excel(dataset_path, engine='openpyxl', dtype=str)
        elif file_extension == '.csv':
            dataset = pd.read_csv(dataset_path, dtype=str, encoding='utf-8')
        else:
            raise ValueError(
                f"Unsupported file format: {file_extension}. Only .xls, .xlsx, or .csv files are supported.")
    except UnicodeDecodeError:
        if file_extension == '.csv':
            dataset = pd.read_csv(dataset_path, dtype=str, encoding='latin1')
        else:
            raise

    branch_code_column = get_branch_code_column(dataset)
    segmented_files = []
    for branch_code, group in dataset.groupby(branch_code_column):
        segment_file_name = f"Branch({branch_code}) - {base_file_name}.xlsx"
        segment_file_path = os.path.join(".\\dataset\\Branch-wise Data Segmentation", segment_file_name)
        group.to_excel(segment_file_path, index=False)
        segmented_files.append(segment_file_path)
    return segmented_files


if __name__ == "__main__":
    dataset_path = sys.argv[1]
    # dataset_path = "C:\\Users\\Md.Mizanur Rahman\\Downloads\\Samity Data Single sheet.xlsx"
    segmented_files = segment_dataset(dataset_path)
    print(segmented_files)  # Return the list of segmented file paths
