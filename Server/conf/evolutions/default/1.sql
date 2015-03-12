# --- !Ups

CREATE TABLE user(
  user_id BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
  phone VARCHAR(32) NOT NULL,
  email VARCHAR(512) NOT NULL,
  password VARCHAR(512) NOT NULL,
  create_date DATETIME not NULL
);

CREATE TABLE expense_list(
  explist_id BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
  creator_ref_id BIGINT NOT NULL,
  name VARCHAR(1024) NOT NULL,
  description TEXT NOT NULL,
  create_date DATETIME NOT NULL,
  FOREIGN KEY (creator_ref_id) REFERENCES user(user_id) ON DELETE CASCADE
);

CREATE TABLE user_expense_list_join(
  user_ref_id BIGINT NOT NULL,
  explist_ref_id BIGINT NOT NULL,
  create_date DATETIME NOT NULL,
  PRIMARY KEY (user_ref_id, explist_ref_id),
  FOREIGN KEY (user_ref_id) REFERENCES user(user_id) ON DELETE CASCADE,
  FOREIGN KEY (explist_ref_id) REFERENCES expense_list(explist_id) ON DELETE CASCADE
);

CREATE TABLE expense(
  exp_id BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
  location VARCHAR(1024) NOT NULL,
  description TEXT NOT NULL,
  parent_ref_id BIGINT NOT NULL,
  creator_ref_id BIGINT NOT NULL,
  category_ref_id BIGINT NOT NULL,
  amount INT NOT NULL,
  create_date DATETIME NOT NULL,
  FOREIGN KEY (creator_ref_id) REFERENCES user(user_id) ON DELETE CASCADE,
  FOREIGN KEY (parent_ref_id) REFERENCES expense_list(explist_id) ON DELETE CASCADE,
  FOREIGN KEY (category_ref_id) REFERENCES expense_list(explist_id)
);

CREATE TABLE user_expense_join(
  user_ref_id BIGINT NOT NULL,
  exp_ref_id BIGINT NOT NULL,
  paid_amount DOUBLE NOT NULL,
  responsible_amount DOUBLE NOT NULL,
  create_date DATETIME NOT NULL,
  PRIMARY KEY (user_ref_id, exp_ref_id),
  FOREIGN KEY (user_ref_id) REFERENCES user(user_id) ON DELETE CASCADE,
  FOREIGN KEY (exp_ref_id) REFERENCES expense(exp_id) ON DELETE CASCADE
);

CREATE TABLE expense_category(
  expcat_id BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
  cat_name VARCHAR(128) NOT NULL,
  create_date DATETIME NOT NULL,
  creator_ref_id BIGINT NOT NULL,
  explist_ref_id BIGINT NOT NULL,
  FOREIGN KEY (creator_ref_id) REFERENCES user(user_id) ON DELETE CASCADE,
  FOREIGN KEY (explist_ref_id) REFERENCES expense_list(explist_id) ON DELETE CASCADE
);

# CREATE TABLE expense_category_join(
#   exp_ref_id BIGINT NOT NULL,
#   expcat_ref_id BIGINT NOT NULL,
#   create_date DATETIME NOT NULL,
#   PRIMARY KEY (exp_ref_id, expcat_ref_id),
#   FOREIGN KEY (exp_ref_id) REFERENCES expense(exp_id) ON DELETE CASCADE,
#   FOREIGN KEY (expcat_ref_id) REFERENCES expense_category(expcat_id) ON DELETE CASCADE
# );

# --- !Downs

# DROP TABLE expense_category_join;
DROP TABLE expense_category;
DROP TABLE user_expense_join;
DROP TABLE expense;
DROP TABLE user_expense_list_join;
DROP TABLE expense_list;
DROP TABLE user;
