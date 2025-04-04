# Human Resource Management System (HRMS)

A desktop application built with JavaFX to manage human resources operations including employee data management, payroll processing, leave management, and performance evaluations.

## Features

### Employee Management
- Create, read, update, and delete employee records
- Track employee information including personal details, job position, and salary
- Search for employees by name, department, or job title

### Leave Management
- Submit leave requests with start and end dates
- Approve or reject leave requests
- Track available leave days for each employee
- View leave history

### Payroll Processing
- Generate payroll entries for individual employees or all employees
- Calculate salary, deductions, and net pay
- Process payroll and mark as paid
- View payroll history

### Performance Evaluations
- Create and manage employee performance reviews
- Rate employees on a 1-5 scale
- Record strengths, areas for improvement, and comments
- Track evaluation history

## Technical Details

- **Language:** Java 21
- **Framework:** JavaFX
- **Data Storage:** In-memory data structures (no database required)
- **Architecture:** Model-View-Controller (MVC) pattern

## Setup Instructions

### Prerequisites
- Java Development Kit (JDK) 21 or later
- Maven

### Running the Application

1. Clone the repository:
   ```
   git clone https://github.com/yourusername/hrsm2.git
   ```

2. Navigate to the project directory:
   ```
   cd hrsm2
   ```

3. Build the project with Maven:
   ```
   mvn clean install
   ```

4. Run the application:
   ```
   mvn javafx:run
   ```

## Usage Guide

### Adding Employees
1. Navigate to the "Employee Management" tab
2. Fill in the employee details in the form at the bottom
3. Click "Add" to create a new employee record

### Managing Leave Requests
1. Navigate to the "Leave Management" tab
2. Select an employee from the dropdown
3. Enter leave details and click "Submit Request"
4. For approving/rejecting requests, select the request from the table and click "Approve" or "Reject"

### Processing Payroll
1. Navigate to the "Payroll Processing" tab
2. To generate a single payroll, select an employee and fill in the details
3. To generate for all employees, click "Generate All Payrolls"
4. Process payrolls by selecting from the table and clicking "Process"
5. Mark processed payrolls as paid using the "Mark as Paid" button

### Evaluating Performance
1. Navigate to the "Performance Evaluations" tab
2. Select an employee and fill in the evaluation details
3. Use the slider to set the performance rating
4. Enter strengths, areas for improvement, and comments
5. Click "Add Evaluation" to save

## License

[MIT License](LICENSE) 