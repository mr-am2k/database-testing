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
def generate_data(num_records=10_000_000, filename='product_10M.csv'):
    fake = Faker()

    header = ['name', 'description', 'startPrice', 'startDate', 'endDate', 'status',
              'categoryName', 'firstName', 'lastName', 'email', 'password', 'userStatus',
              'address', 'city', 'country', 'zipCode', 'nameOnTheCard', 'cardNumber',
              'cvv', 'expirationDate']

    categories = ['Electronics', 'Books', 'Clothing', 'Home', 'Sports', 'Toys']
    statuses = ['ACTIVE', 'INACTIVE', 'PENDING']
    user_statuses = ['VERIFIED', 'UNVERIFIED', 'SUSPENDED']

    # Calculate date ranges
    current_date = datetime.now()
    start_range = current_date - timedelta(days=365 * 2.5)  # 2.5 years back
    end_range = current_date + timedelta(days=365 * 2.5)  # 2.5 years forward

    with open(filename, 'w', newline='', encoding='utf-8') as file:
        writer = csv.writer(file)
        writer.writerow(header)

        for _ in tqdm(range(num_records)):
            start_date = fake.date_time_between(start_date=start_range, end_date=end_range)
            end_date = start_date + timedelta(days=randint(1, 365))

            # Format dates to match Java's LocalDateTime format
            start_date_formatted = start_date.strftime('%Y-%m-%dT%H:%M:%S')
            end_date_formatted = end_date.strftime('%Y-%m-%dT%H:%M:%S')

            writer.writerow([
                fake.word().capitalize() + " " + fake.word().capitalize(),
                fake.text(max_nb_chars=200),
                round(uniform(10, 1000), 2),
                start_date_formatted,
                end_date_formatted,
                choice(statuses),
                choice(categories),
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
                fake.credit_card_expire()
            ])


if __name__ == '__main__':
    generate_data()