import os
import sys
from pathlib import Path
import pandas as pd


def get_branch_code_column(columns):
    possible_names = [
        'Branch Code', 'Branch ID', 'BranchCode', 'BranchID', 'BCode', 'BID',
        'Branch Code No', 'Branch Code Number', 'Branch ID No', 'Branch ID Number',
        'Branch No', 'Branch Number', 'Branch', 'BranchCode', 'BranchID'
    ]
    for col in columns:
        if col in possible_names:
            return col
    raise ValueError("Branch Code column not found.")


def segment_dataset(dataset_path):
    file_extension = Path(dataset_path).suffix.lower()
    base_file_name = Path(dataset_path).stem
    output_dir = Path(".\\dataset\\Branch-wise Data Segmentation")
    # Ensure the output directory exists
    output_dir.mkdir(parents=True, exist_ok=True)

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
                segment_file_name = f"Branch({branch_code}) - {base_file_name}.xlsx"
                segment_file_path = output_dir / segment_file_name
                if segment_file_path.exists():
                    group.to_excel(segment_file_path, index=False, header=False, mode='a')
                else:
                    group.to_excel(segment_file_path, index=False)
                if segment_file_name not in segment_file_names:
                    segment_file_names.append(segment_file_name)
    else:
        for sheet_name, data in reader.items():
            data = data.loc[:, ~data.columns.str.contains('^Unnamed')]  # Remove any unwanted 'Unnamed' columns
            branch_code_column = get_branch_code_column(data.columns)
            for branch_code, group in data.groupby(branch_code_column):
                segment_file_name = f"Branch({branch_code}) - {base_file_name}_{sheet_name}.xlsx"
                segment_file_path = output_dir / segment_file_name
                if segment_file_path.exists():
                    group.to_excel(segment_file_path, index=False, header=False, mode='a')
                else:
                    group.to_excel(segment_file_path, index=False)
                if segment_file_name not in segment_file_names:
                    segment_file_names.append(segment_file_name)

    return segment_file_names


if __name__ == "__main__":
    dataset_path = sys.argv[1]
    # dataset_path = "C:\\Users\\Md.Mizanur Rahman\\Downloads\\DSK Member Data.xlsx"
    segment_file_names = segment_dataset(dataset_path)
    for file in segment_file_names:
        print(file)