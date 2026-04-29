import pymysql
import random
import datetime
from faker import Faker

fake = Faker('zh_CN')

def init_db():
    conn = pymysql.connect(
        host='localhost',
        user='root',
        password='jiaqi.Guo5946',
        database='fund_together',
        charset='utf8mb4',
        cursorclass=pymysql.cursors.DictCursor
    )
    
    with conn.cursor() as cursor:
        # 1. Create Users
        users = []
        for i in range(10):
            account = fake.user_name()
            nickname = fake.name()
            phone = fake.phone_number()
            balance = random.randint(1000, 10000)
            # Default password can be hashed later, for now just a dummy string
            cursor.execute(
                "INSERT INTO sys_user (account, password, nickname, phone, role, status, balance, gender, location, company, profession) "
                "VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)",
                (account, '123456', nickname, phone, 1, 1, balance, random.choice([0,1,2]), fake.city(), fake.company(), fake.job())
            )
            users.append(cursor.lastrowid)
            
        conn.commit()
        print(f"Created {len(users)} users.")

        # 2. Get existing projects
        cursor.execute("SELECT id, target_amount FROM project WHERE status = 1")
        projects = cursor.fetchall()
        
        # 3. Create support orders and funding ledgers
        orders_created = 0
        for project in projects:
            project_id = project['id']
            # 2 to 5 supports per project
            num_supports = random.randint(2, 5)
            total_amount = 0
            
            for _ in range(num_supports):
                user_id = random.choice(users)
                amount = random.randint(10, 500)
                total_amount += amount
                
                order_no = f"SO{datetime.datetime.now().strftime('%Y%m%d%H%M%S')}{random.randint(1000,9999)}"
                
                # Insert order
                cursor.execute(
                    "INSERT INTO support_order (order_no, user_id, project_id, amount, message, status, pay_channel, pay_time) "
                    "VALUES (%s, %s, %s, %s, %s, %s, %s, NOW())",
                    (order_no, user_id, project_id, amount, fake.sentence(), 1, 'BALANCE')
                )
                order_id = cursor.lastrowid
                
                # Insert funding ledger (Type 1: income)
                cursor.execute(
                    "INSERT INTO funding_ledger (project_id, order_id, user_id, amount, type, status, remark) "
                    "VALUES (%s, %s, %s, %s, %s, %s, %s)",
                    (project_id, order_id, user_id, amount, 1, 1, "User Support")
                )
                orders_created += 1
            
            # Update project amounts
            cursor.execute(
                "UPDATE project SET current_amount = current_amount + %s, supporter_count = supporter_count + %s WHERE id = %s",
                (total_amount, num_supports, project_id)
            )
            
        conn.commit()
        print(f"Created {orders_created} support orders and funding ledgers.")
        
    conn.close()

if __name__ == '__main__':
    init_db()
