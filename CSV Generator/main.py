import csv
from datetime import datetime, timedelta
from bson.objectid import ObjectId
from random import randint, choice, uniform

from faker import Faker
from tqdm import tqdm

#
# def generate_address_data(num_records=5_000_000, filename='addresses_5M.csv'):
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

def generate_data(num_records=5_000_000, filename='users_5M.csv'):
    fake = Faker()

    header = ['firstName', 'lastName', 'email', 'password', 'userStatus',
              'address', 'city', 'country', 'zipCode', 'nameOnTheCard', 'cardNumber',
              'cvv', 'expirationDate']

    user_statuses = ['VERIFIED', 'UNVERIFIED', 'SUSPENDED']

    # Calculate date ranges
    current_date = datetime.now()
    start_range = current_date - timedelta(days=365 * 2.5)
    end_range = current_date + timedelta(days=365 * 2.5)

    with open(filename, 'w', newline='', encoding='utf-8') as file:
        writer = csv.writer(file)
        writer.writerow(header)

        for _ in tqdm(range(num_records)):
            # Generate date in ISO-8601 format (YYYY-MM-DD) without time information
            expiration_date = fake.date_time_between(
                start_date=start_range,
                end_date=end_range
            ).strftime('%Y-%m-%d')

            writer.writerow([
                fake.first_name(),
                fake.last_name(),
                fake.email(),
                fake.password(length=12),
                choice(user_statuses),
                fake.street_address(),
                fake.city(),
                fake.country(),
                fake.postcode(),
                fake.name(),
                fake.credit_card_number(),
                fake.credit_card_security_code(),
                expiration_date  # Now in YYYY-MM-DD format
            ])

if __name__ == '__main__':
    generate_data()
