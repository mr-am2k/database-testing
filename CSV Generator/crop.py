import pandas as pd

# Load the users CSV file
users_file = "/Users/muamer/Desktop/muamer/master-thesis/database-testing/CSV Generator/addresses_5m_3.csv"
df = pd.read_csv(users_file)

# Extract relevant address data
address_df = df[["address", "city", "country", "zipCode"]]

# Define chunk size
chunk_size = 1_000_000

# Split data into two files
for i, chunk in enumerate(range(0, len(address_df), chunk_size), start=1):
    chunk_df = address_df.iloc[chunk:chunk + chunk_size]
    output_file = f"addresses_5m_4_{i}.csv"
    chunk_df.to_csv(output_file, index=False)
    print(f"Saved {len(chunk_df)} records to {output_file}")
