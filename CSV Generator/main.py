import csv
from datetime import datetime, timedelta
from random import randint, choice, uniform

from faker import Faker
from tqdm import tqdm

# Addresses
# def generate_address_data(num_records=10_000_000, filename='addresses.csv'):
#     fake = Faker()
#
#     header = ['address', 'city', 'country', 'zipCode']
#
#     with open(filename, 'w', newline='', encoding='utf-8') as file:
#         writer = csv.writer(file)
#         writer.writerow(header)
#
#         for _ in tqdm(range(num_records)):
#             writer.writerow([
#                 fake.street_address(),
#                 fake.city(),
#                 fake.country(),
#                 fake.postcode()
#             ])
#
#
# if __name__ == '__main__':
#     generate_address_data()

# Products
def generate_data(num_records=10_000_000, filename='users_10M.csv'):
    fake = Faker()

    header = ['firstName', 'lastName', 'email', 'password', 'userStatus',
              'address', 'city', 'country', 'zipCode', 'nameOnTheCard', 'cardNumber',
              'cvv', 'expirationDate']

    user_statuses = ['VERIFIED', 'UNVERIFIED', 'SUSPENDED']

    # Calculate date ranges
    current_date = datetime.now()
    start_range = current_date - timedelta(days=365 * 2.5)  # 2.5 years back
    end_range = current_date + timedelta(days=365 * 2.5)  # 2.5 years forward

    def short_string(string, length=10):
        return string[:length]  # Skraćuje string na dužinu 10 karaktera

    with open(filename, 'w', newline='', encoding='utf-8') as file:
        writer = csv.writer(file)
        writer.writerow(header)

        for _ in tqdm(range(num_records)):
            start_date = fake.date_time_between(start_date=start_range, end_date=end_range)
            end_date = start_date + timedelta(days=randint(1, 365))

            # Format dates to match Java's LocalDateTime format
            start_date_formatted = start_date.strftime('%Y-%m-%dT%H:%M:%S')
            end_date_formatted = end_date.strftime('%Y-%m-%dT%H:%M:%S')

            # Kreiramo CSV red sa skraćenim stringovima
            writer.writerow([
                short_string(fake.first_name()),  # Ime
                short_string(fake.last_name()),  # Prezime
                short_string(fake.email()),  # Email
                short_string(fake.password(length=12)),  # Lozinka
                short_string(choice(user_statuses)),  # Status korisnika
                short_string(fake.street_address()),  # Adresa
                short_string(fake.city()),  # Grad
                short_string(fake.country()),  # Država
                short_string(fake.postcode()),  # Poštanski broj
                short_string(fake.name()),  # Ime na kartici
                short_string(fake.credit_card_number()),  # Broj kreditne kartice
                short_string(fake.credit_card_security_code()),  # CVV
                short_string(fake.credit_card_expire())  # Datum isteka kartice
            ])

if __name__ == '__main__':
    generate_data()