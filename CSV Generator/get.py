import pandas as pd

# Putanja do vašeg CSV fajla
putanja_do_csv = '/Users/muamer/Desktop/muamer/master-thesis/database-testing/src/main/resources/address/addresses_5M.csv'

# Učitavanje CSV fajla u DataFrame
df = pd.read_csv(putanja_do_csv)

# Broj rekorda u CSV fajlu (ne računajući header ako postoji)
broj_rekorda = len(df)

print(f"Broj rekorda u CSV fajlu je: {broj_rekorda}")
