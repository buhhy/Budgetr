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
  creator_id BIGINT NOT NULL,
  name VARCHAR(1024) NOT NULL,
  description TEXT NOT NULL,
  create_date DATETIME NOT NULL,
  FOREIGN KEY (creator_id) REFERENCES user(user_id) ON DELETE CASCADE
);

CREATE TABLE user_expense_join(
  user_id BIGINT NOT NULL,
  explist_id BIGINT NOT NULL,
  create_date DATETIME NOT NULL,
  PRIMARY KEY (user_id, explist_id),
  FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
  FOREIGN KEY (explist_id) REFERENCES expense_list(explist_id) ON DELETE CASCADE
);

CREATE TABLE expense(
  exp_id BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT,
  location VARCHAR(1024) NOT NULL,
  description TEXT NOT NULL,
  parent_id BIGINT NOT NULL,
  creator_id BIGINT NOT NULL,
  amount INT NOT NULL,
  create_date DATETIME NOT NULL,
  FOREIGN KEY (creator_id) REFERENCES user(user_id) ON DELETE CASCADE,
  FOREIGN KEY (parent_id) REFERENCES expense_list(explist_id) ON DELETE CASCADE
);

# --- !Downs

DROP TABLE expense;
DROP TABLE user_expense_join;
DROP TABLE expense_list;
DROP TABLE user;
