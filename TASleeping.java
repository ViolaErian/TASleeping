package tasleeping;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Semaphore;

public class TASleeping extends JFrame {

    private Semaphore TA;
    private Semaphore Student;
    private Semaphore Access;
    private int NumOfEmptyChairs;
    private int waitingStudentsCount = 0; // Counter for waiting students
    private int leftStudentsCount = 0; // Counter for students who have left

    private JTextArea outputArea;
    private JTextField chairsInput;
    private JTextField taInput;
    private JTextField studentInput;

    public TASleeping() {
        setTitle("TA Sleeping with GUI");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        outputArea = new JTextArea();
        outputArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(outputArea);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(4, 2));

        inputPanel.add(new JLabel("Number of Chairs:"));
        chairsInput = new JTextField();
        inputPanel.add(chairsInput);

        inputPanel.add(new JLabel("Number of TAs:"));
        taInput = new JTextField();
        inputPanel.add(taInput);

        inputPanel.add(new JLabel("Number of Students:"));
        studentInput = new JTextField();
        inputPanel.add(studentInput);

        JButton startButton = new JButton("Start Simulation");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startSimulation();
            }
        });

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(startButton, BorderLayout.SOUTH);

        add(panel);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void startSimulation() {
        try {
            NumOfEmptyChairs = Integer.parseInt(chairsInput.getText());
            int numTAs = Integer.parseInt(taInput.getText());
            int numStudents = Integer.parseInt(studentInput.getText());

            TA = new Semaphore(numTAs);
            Student = new Semaphore(0);
            Access = new Semaphore(1);

            Teacher teacher = new Teacher();
            teacher.start();

            for (int u = 1; u <= numStudents; u++) {
                Student newStudent = new Student(u);
                newStudent.start();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        } catch (NumberFormatException ex) {
            appendToOutput("Invalid input. Please enter valid numbers.");
        }
    }

    class Student extends Thread {
        int A;
        boolean notAsk = true;

        public Student(int u) {
            A = u;
        }

        public void run() {
            while (notAsk) {
                try {
                    Access.acquire();
                    if (NumOfEmptyChairs > 0) {
                        appendToOutput("Student " + this.A + " took a Seat. Waiting Students: " + waitingStudentsCount + ", Left Students: " + leftStudentsCount);
                        NumOfEmptyChairs--;
                        Access.release();
                        Student.release();
                        waitingStudentsCount++;
                        try {
                            TA.acquire();
                            notAsk = false;
                            this.Get_AskTA();
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    } else {
                        appendToOutput("There are no free seats for student " + this.A + ". Waiting Students: " + waitingStudentsCount + ", Left Students: " + leftStudentsCount);
                        Access.release();
                        notAsk = false;
                        leftStudentsCount++;
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }

        public void Get_AskTA() {
            appendToOutput("Student " + this.A + " is Asking TA. Waiting Students: " + waitingStudentsCount + ", Left Students: " + leftStudentsCount);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            } finally {
                TA.release();
                waitingStudentsCount--;
            }
        }
    }

    class Teacher extends Thread {
        public Teacher() {
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Student.acquire();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                NumOfEmptyChairs++;
                TA.release();
                Access.release();
                AskTA();
            }
        }

        public void AskTA() {
            appendToOutput("The TA is Answering a student. Waiting Students: " + waitingStudentsCount + ", Left Students: " + leftStudentsCount);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void appendToOutput(String text) {
        SwingUtilities.invokeLater(() -> outputArea.append(text + "\n"));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TASleeping());
    }
}
