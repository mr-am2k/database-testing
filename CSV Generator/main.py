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
    start_range = current_date - timedelta(days=365 * 2.5)
    end_range = current_date + timedelta(days=365 * 2.5)

    def short_string(string, length=10):
        return string[:length]

    with open(filename, 'w', newline='', encoding='utf-8') as file:
        writer = csv.writer(file)
        writer.writerow(header)

        for _ in tqdm(range(num_records)):
            start_date = fake.date_time_between(start_date=start_range, end_date=end_range)
            end_date = start_date + timedelta(days=randint(1, 365))

            start_date_formatted = start_date.strftime('%Y-%m-%dT%H:%M:%S')
            end_date_formatted = end_date.strftime('%Y-%m-%dT%H:%M:%S')

            writer.writerow([
                short_string(fake.first_name()),
                short_string(fake.last_name()),
                short_string(fake.email()),
                short_string(fake.password(length=12)),
                short_string(choice(user_statuses)),
                short_string(fake.street_address()),
                short_string(fake.city()),
                short_string(fake.country()),
                short_string(fake.postcode()),
                short_string(fake.name()),
                short_string(fake.credit_card_number()),
                short_string(fake.credit_card_security_code()),
                short_string(fake.credit_card_expire()) 
            ])

if __name__ == '__main__':
    generate_data()