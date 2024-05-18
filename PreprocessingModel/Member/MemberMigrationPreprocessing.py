import sys

import pandas as pd
from sklearn.cluster import KMeans


def preprocess_excel(input_path):
    # Load dataset
    df = pd.read_excel(input_path)  # Adjust this line to parse Excel files

    # Drop null rows
    df.dropna(inplace=True)

    # Select the required columns
    columns_to_keep = ['Name', 'Admission Date', 'Primary Product', 'Samity', 'Age']
    df_sample = df[columns_to_keep].copy()

    # Perform clustering on numeric columns
    numeric_columns = df_sample.select_dtypes(include=['int', 'float']).columns
    if not numeric_columns.empty:
        n_samples = len(df_sample)
        n_clusters = min(3, n_samples)  # Adjust number of clusters if necessary
        if n_samples >= n_clusters:
            kmeans = KMeans(n_clusters=n_clusters)
            df_sample['cluster'] = kmeans.fit_predict(df_sample[numeric_columns])
        else:
            print("Number of samples is less than the number of clusters. Adjusting the number of clusters.")
            kmeans = KMeans(n_clusters=n_samples)
            df_sample['cluster'] = kmeans.fit_predict(df_sample[numeric_columns])

    # Save preprocessed dataset to Excel
    # df_sample.to_excel('preprocessed_dataset.xlsx', index=False)
    # Write processed data back to Excel file
    # output_path = input_path.replace("uploads", "processed")
    # output_path = "C:\\Users\\Md.Mizanur Rahman\\Documents\\mL\\dataset\\processed-data.xlsx"
    output_path = ".\\dataset\\processed\\Member Migration processed dataset.xlsx"
    df_sample.to_excel(output_path, index=False)
    print(output_path)

    return df_sample


if __name__ == "__main__":
    input_path = "D:\Automation\DataShift\dataset\unprocessed\Member's Information.xlsx"
    # input_path = sys.argv[1]
    preprocess_excel(input_path)
    preprocessed_df = preprocess_excel(input_path)
    print("Preprocessed dataset:")
    print(preprocessed_df)
# Example usage
# dataset_path = '/content/Member.xlsx'  # Provide the path to your Excel dataset
# preprocessed_df = preprocess_data(dataset_path)
# print("Preprocessed dataset:")
# print(preprocessed_df)
