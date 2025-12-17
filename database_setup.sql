-- Create database tables for the roll call system
-- 脆鼠修改！⚠️
USE software_project;

-- Create student table
CREATE TABLE IF NOT EXISTS student (
    student_id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    gender VARCHAR(10),
    class VARCHAR(50),
    photo_path VARCHAR(255)
);

-- Create session table
CREATE TABLE IF NOT EXISTS session (
    session_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    date TIMESTAMP NOT NULL,
    call_type ENUM('ALL', 'RANDOM') NOT NULL,
    selected_count INT,
    strategy ENUM('RANDOM', 'MOST_ABSENT', 'LEAST_CALLED') NOT NULL
);

-- Create record table
CREATE TABLE IF NOT EXISTS record (
    record_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    student_id VARCHAR(50) NOT NULL,
    attendance_status ENUM('PENDING', 'ATTEND', 'LEAVE', 'ABSENT', 'LATE') NOT NULL DEFAULT 'PENDING',
    call_time TIMESTAMP NOT NULL,
    response_time TIMESTAMP,
    late_time INT,
    FOREIGN KEY (session_id) REFERENCES session(session_id),
    FOREIGN KEY (student_id) REFERENCES student(student_id)
);

-- Create stat table
CREATE TABLE IF NOT EXISTS stat (
    student_id VARCHAR(50) PRIMARY KEY,
    total_calls INT DEFAULT 0,
    attendance_count INT DEFAULT 0,
    leave_count INT DEFAULT 0,
    absence_count INT DEFAULT 0,
    late_count INT DEFAULT 0,
    FOREIGN KEY (student_id) REFERENCES student(student_id)
);

-- Insert or update the actual student data
INSERT INTO student (student_id, name, gender, class, photo_path) VALUES
('2023141460001', '王以太', '男', '行政1班', 'students_picture/wang_yitai.jpg'),
('2023141460220', '肖德俊', '男', '行政3班', 'students_picture/xiao_dejun.jpg'),
('2023141460328', '周延', '男', '行政1班', 'students_picture/zhou_yan.jpg'),
('2023141460303', '刘聪', '男', '行政2班', 'students_picture/KeyL.jpg'),
('2023141460038', '功夫胖', '男', '行政2班', 'students_picture/gongfupang.jpg'),
('2023141460005', '盛宇', '男', '行政2班', 'students_picture/shengyu.jpg'),
('2023141460006', '马思唯', '男', '行政3班', 'students_picture/masiwei.jpg'),
('2023141460007', 'KnowKnow', '男', '行政3班', 'students_picture/knowknow.jpg'),
('2023141460008', '杨俊逸', '男', '行政3班', 'students_picture/psyp.jpg'),
('2023141460009', 'Asen', '男', '行政4班', 'students_picture/asen.jpg'),
('2023141460017', '杨和苏', '男', '行政5班', 'students_picture/yang_hesu.jpg'),
('2023141460011', '法老', '男', '行政6班', 'students_picture/pharaoh.jpg'),
('2023141460342', 'Shark', '女', '行政7班', 'students_picture/shark.jpg'),
('2023141460143', 'GALI', '男', '行政7班', 'students_picture/gali.jpg'),
('2023141460063', '翁颖', '女', '行政7班', 'students_picture/wengying.jpg'),
('2023141460021', '黄之仪', '女', '行政7班', 'students_picture/huang_zhiyi.jpg')
ON DUPLICATE KEY UPDATE 
    name = VALUES(name), 
    gender = VALUES(gender), 
    class = VALUES(class), 
    photo_path = VALUES(photo_path);