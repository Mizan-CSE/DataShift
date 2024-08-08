import os
import sys
from pathlib import Path
import pandas as pd


def get_branch_code_column(columns):
    possible_name = [
        'Branch Code', 'Branch ID', 'BranchCode', 'BranchID', 'BCode', 'BID',
        'Branch Code No', 'Branch Code Number', 'Branch ID No', 'Branch ID Number',
        'Branch No', 'Branch Number', 'Branch'
    ]
    for col in columns:
        if col in possible_name:
            return col
    raise ValueError("Branch Code column not found.")


def segment_dataset(dataset_path):
    file_extension = Path(dataset_path).suffix.lower()
    base_file_name = Path(dataset_path).stem

    if file_extension == '.csv':
        reader = pd.read_csv(dataset_path, dtype=str, keep_default_na=False, na_values=[''])
    elif file_extension in ['.xls', '.xlsx']:
        reader = pd.read_excel(dataset_path, dtype=str, sheet_name=None, keep_default_na=False, na_values=[''])
    else:
        raise ValueError(f"Unsupported file format: {file_extension}. Only .xls, .xlsx, or .csv files are supported.")

    segment_file_names = []

    if file_extension == '.csv':
        first_chunk = True
        for chunk in pd.read_csv(dataset_path, dtype=str, chunksize=100000, keep_default_na=False, na_values=['']):
            if first_chunk:
                branch_code_column = get_branch_code_column(chunk.columns)
                first_chunk = False

            for branch_code, group in chunk.groupby(branch_code_column):
                # Added lines to create folder path and ensure it exists
                folder_path = os.path.join(".\\dataset\\Branch-wise Data Segmentation", f"Branch({branch_code})")
                os.makedirs(folder_path, exist_ok=True)
                # Updated segment file path to include the folder path
                segment_file_name = f"Branch({branch_code}) - {base_file_name}.xlsx"
                segment_file_path = os.path.join(folder_path, segment_file_name)
                if os.path.exists(segment_file_path):
                    group.to_excel(segment_file_path, index=False, header=False, mode='a')
                else:
                    group.to_excel(segment_file_path, index=False)
                if segment_file_path not in segment_file_names:
                    segment_file_names.append(segment_file_path)
    else:
        for sheet_name, data in reader.items():
            data = data.loc[:, ~data.columns.str.contains('^Unnamed')]  # Remove any unwanted 'Unnamed' columns
            branch_code_column = get_branch_code_column(data.columns)
            for branch_code, group in data.groupby(branch_code_column):
                # Added lines to create folder path and ensure it exists
                folder_path = os.path.join(".\\dataset\\Branch-wise Data Segmentation", f"Branch({branch_code})")
                os.makedirs(folder_path, exist_ok=True)
                # Updated segment file path to include the folder path
                segment_file_name = f"Branch({branch_code}) - {sheet_name}.xlsx"
                segment_file_path = os.path.join(folder_path, segment_file_name)
                if os.path.exists(segment_file_path):
                    group.to_excel(segment_file_path, index=False, header=False, mode='a')
                else:
                    group.to_excel(segment_file_path, index=False)
                if segment_file_path not in segment_file_names:
                    segment_file_names.append(segment_file_path)

    return segment_file_names


if __name__ == "__main__":
    dataset_path = sys.argv[1]
    # dataset_path = "C:\\Users\\hp\\Downloads\\Data Migration Template for DSK - Copy.xlsx"
    root = ".\\dataset\\Branch-wise Data Segmentation"
    os.makedirs(root, exist_ok=True)
    segment_file_names = segment_dataset(dataset_path)
    for file in segment_file_names:
        print(file)
