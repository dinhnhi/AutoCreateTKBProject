import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;

public class Main {
    Set<String> idSubjectList;
    ArrayList<TKB> tkbList;
    Map sourceMap;

    XSSFWorkbook workbook;
    ArrayList<String> hauToList; // khong nho tieng anh la chu nao
    File idSubjectFile;
    File sourceExcelFile;

    JFrame frame;
    JPanel centerPanel;
    JTextArea infoArea;
    JScrollPane southSrollListTKB;

    public static void main(String[] args){
        Main app = new Main();
        app.go();

    }

    public void go(){
        loadLelfPanel();

        frame.setVisible(true);
    }

    public Main(){
        idSubjectList = new HashSet<>();
        tkbList = new ArrayList<>();

        frame = new JFrame();
        frame.setSize(1000, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        centerPanel = new JPanel();
        southSrollListTKB = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        frame.add(BorderLayout.CENTER, centerPanel);
        frame.add(BorderLayout.SOUTH, southSrollListTKB);

        frame.revalidate();
        frame.repaint();
    }
    public Main(HashSet<String> id){
        this();
        idSubjectList = id;
    }
    public void setIdSubjectList(HashSet<String> id){
        idSubjectList = id;
    }

    private void loadIdSubjectFile(File file){
        printInfo("Start load your subject file");
        sourceMap = new HashMap<String, ArrayList<Subject>>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = "";
            while ((line = reader.readLine()) != null) {
                idSubjectList.add(line);
                sourceMap.put(line, new ArrayList<Subject>());
            }
        }
        catch (IOException ex){
            printInfo("Cannot open your subject file");
        }
        printInfo("Done load your subject file");
    }
    private void loadExcelSourceFile(File file) throws IOException { // can optomize
        printInfo("Start Load Source!");

        FileInputStream input = new FileInputStream(file);
        XSSFWorkbook workbook = new XSSFWorkbook(input);

        String nameSheetLT = "TKB LT";
        String nameSheetTH = "TKB TH";

        // readWorkSheetLT phải gọi trước readWorkSheetTH
        // vì danh sách lớp học thực hành lấy từ hàm readWorkSheetTH
        // dựa trên danh sách lớp học lý thuyết lấy từ hàm readWorkSheetLT
        readWorkSheetLT(workbook, nameSheetLT);
        readWorkSheetTH(workbook, nameSheetTH);

        printInfo("Done Load Source!");
    }
    private void readWorkSheetLT(XSSFWorkbook workbook, String nameSheet){
        XSSFSheet sheet = workbook.getSheet(nameSheet);

        Iterator<Row> rows = sheet.rowIterator();
        int[] indexCell = new int[]{1,2,3,7, 10,11}; // Theo định dạng file excel của UIT thì mảng trên tương ứng với các cột
        while(rows.hasNext()){                    // Mã MH, Mã Lớp, Tên Môn Học, số TC, Thứ, Tiết
            Row row = rows.next();
            String[] result = readRow(row, indexCell);

            String idSubject = result[0] ;
            ArrayList<Subject> listSubject = (ArrayList<Subject>) sourceMap.get(idSubject);
            if(listSubject == null) continue; // id mon hoc khong nam trong danh sach yeu cau

            String idClass = result[1];
            String hauTo = "";
            String[] splitIdClass = idClass.split("\\.");
            if(splitIdClass.length < 3)// theo như định dạng mã lớp học của UIT thì nếu mã môn hoc chỉ có 2 thành phần
                hauTo = "NONE";        // là môn học chung cho tất cả các ngành => không có hậu tố
            else hauTo = splitIdClass[2];

            if(! hauToList.contains(hauTo))
                continue;

            String dateOfWork = result[4];
            String time = result[5];
            Lesson lesson = new Lesson(dateOfWork, time);

            boolean isExistClass = false;
            for(Subject subject : listSubject) // Thêm lesson vào môn học nếu môn học tồn tại
                if(subject.idClass.equals(idClass)) {
                    subject.addLessonLT(lesson);
                    isExistClass = true;
                    break;
                }

            if(!isExistClass) {
                Subject subject = new Subject(idClass, lesson);
                subject.setTc_lt(Integer.parseInt(result[3])); // set tc lt ở đây vì trong trường hợp 1 môn có nhiều lớp lt thì chỉ thêm 1 lần
                listSubject.add(subject);
            }
        }
    }
    private void readWorkSheetTH(XSSFWorkbook workbook, String nameSheet){
        XSSFSheet sheet = workbook.getSheet(nameSheet);

        Iterator<Row> rows = sheet.rowIterator();
        int[] indexCell = new int[]{1,2,3,7,10,11}; // Theo định dạng file excel của UIT thì mảng trên tương ứng với các cột
        while(rows.hasNext()){                    // Mã MH, Mã Lớp, Tên Môn Học, số TC, Thứ, Tiết
            Row row = rows.next();
            String[] result = readRow(row, indexCell);

            String idSubject = result[0] ;
            ArrayList<Subject> listSubject = (ArrayList<Subject>) sourceMap.get(idSubject);
            if(listSubject == null) continue; // id môn học đàn xét không nằm trong danh sách lớp yêu cầu

            String idClassTH = result[1];
            String dateOfWork = result[4];
            String time = result[5];
            Lesson lesson = new Lesson(dateOfWork, time);

            String idClassLT = idClassTH.substring(0, idClassTH.length()-2);
            for(Subject subject : listSubject) { // Thêm lớp thực hành trương ứng với lớp lý thuyết đã yêu cầu
                if (subject.idClass.equals(idClassLT)) {
                    subject.setTc_th(Integer.parseInt(result[3]));
                    subject.addLessonTH(lesson);
                    break;
                }

            }
        }
    }
    String[] readRow(Row row, int[] cells){
        String[] result = new String[cells.length];

        for(int i=0; i<cells.length; i++){
            Cell c = row.getCell(cells[i]);
            CellType type = c.getCellType();

            switch (type){
                case _NONE: case BLANK: result[i] = ""; break;
                case BOOLEAN: result[i] = String.valueOf(c.getBooleanCellValue());  break;
                case NUMERIC:  result[i] = String.valueOf((int)c.getNumericCellValue()); break;
                case STRING: result[i] = c.getStringCellValue(); break;
                case ERROR: result[i] = "!"; break;
                case FORMULA:
                    FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
                    // In ra giá trị từ công thức
                    result[i] = String.valueOf((int)evaluator.evaluate(c).getNumberValue());
                    break;
            }
        }

        return result;
    }

    void loadLelfPanel(){
        JPanel lelfPanel = new JPanel();

        JLabel label = new JLabel("Hãy lựa chọn chương trình đạo tạo:");
        JPanel labelPanel = new JPanel();
        labelPanel.add(label);

        Box boxLayout = new Box(BoxLayout.Y_AXIS);
        boxLayout.add(labelPanel);
        boxLayout.add(educationProgramBox());

        lelfPanel.add(boxLayout);
        lelfPanel.updateUI();

        frame.getContentPane().add(BorderLayout.WEST, lelfPanel);
        frame.repaint();
    }
    void loadRightPanel(){
        JPanel rigthPanel = new JPanel();

        Box boxLayout = new Box(BoxLayout.Y_AXIS);
        boxLayout.add(loadStartButton(rigthPanel));
        boxLayout.add(loadInfoArea());

        rigthPanel.add(boxLayout);
        frame.add(BorderLayout.EAST, rigthPanel);
        frame.revalidate();
        frame.repaint();
    }

    private void loadMenuBar(){
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Open");
        JMenuItem fileItem = new JMenuItem("File chứa danh sách mã môn học bạn muốn đăng kí");
        JMenuItem sourceItem = new JMenuItem("File excel chứa thời khóa biểu các môn ");

        fileItem.addActionListener(new FileItemActionListener(sourceItem));
        sourceItem.addActionListener(new SourceItemActionListen());

        sourceItem.setEnabled(false);

        fileMenu.add(fileItem);
        fileMenu.add(sourceItem);


        menuBar.add(fileMenu);
        frame.getContentPane().add(BorderLayout.NORTH, menuBar);
        frame.revalidate();
        frame.repaint();
    }
    private JButton loadStartButton(JPanel panel){
        JButton startButton = new JButton("Start");

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(idSubjectFile == null){
                    printInfo("Donnot have data id subject");
                    return;
                }
                if(sourceExcelFile == null){
                    printInfo("Donnot have data source");
                    return;
                }
                startButton.setEnabled(false);

                findValidTKB();
                loadListTKB(southSrollListTKB);

                ShowComboBoxSort(panel);
            }
        });

        return startButton;
    }
    private JScrollPane loadInfoArea(){
        infoArea = new JTextArea(20, 20);
        infoArea.setFont(new Font("arial", Font.LAYOUT_LEFT_TO_RIGHT, 14));
        infoArea.setEditable(false);

        JScrollPane scroll = new JScrollPane(infoArea);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setAutoscrolls(true);

        return scroll;
    }
    Box educationProgramBox(){
        hauToList = new ArrayList<String>();
        String[] checkboxsName = new String[]{"ATCL", "ANTN", "ANTT", "CNCL",
                "CTTT", "HTCL", "MTCL", "MMCL",
                "PMCL", "KHCL", "KHBC", "TMCL", "NONE"};

        ArrayList<JCheckBox> checkboxList = new ArrayList<JCheckBox>(); // dung de load toan bo checkbox khi button "Done" duoc nhan
        JPanel gridPanel = new JPanel(new GridLayout(4, 4)); // dung de xep cac checkBox

        for(String name: checkboxsName){
            JCheckBox checkBox = new JCheckBox(name);
            checkBox.addActionListener(new CheckBoxActionListener());

            checkboxList.add(checkBox);
            gridPanel.add(checkBox);
        }

        JButton doneButton = new JButton("Done");
        doneButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doneButton.setEnabled(false);
                loadMenuBar();
                loadRightPanel();
                for(JCheckBox checkBox : checkboxList)
                    checkBox.setEnabled(false);
            }
        });

        Box boxLayout = new Box(BoxLayout.Y_AXIS);
        boxLayout.add(gridPanel);
        boxLayout.add(doneButton);

        return boxLayout;
    }
    private JPanel loadInfoTKBPanel(TKB tkb){
        JPanel panel = new JPanel();

        JTextArea listClassArea = new JTextArea();
        listClassArea.setFont(new Font("arial", Font.LAYOUT_LEFT_TO_RIGHT, 14));
        listClassArea.setEditable(false);
        listClassArea.setText("Tổng tín chỉ: " + tkb.getSoTC() + '\n');

        for(String str : tkb.getListClass())
            listClassArea.append(str + '\n');

        JScrollPane scroll = new JScrollPane(listClassArea);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        panel.add(scroll);
        return panel;
    }
    void ShowComboBoxSort(JPanel panel){
        JComboBox comboBox = new JComboBox(new String[]{
                "None",
                "Có nhiều ngày nghỉ cả ngày",
                "Có nhiều  ngày nghỉ vào buổi sáng",
                "Có nhiều ngày nghỉ vào buổi chiều",
                "có nhiều buổi nghỉ"});

        comboBox.addActionListener(new ComboBoxActionListener());

        Component[] listComponent = panel.getComponents();

        JLabel label = new JLabel("Sắp xếp theo:");
        JPanel labelPanel = new JPanel();
        labelPanel.add(label);

        Box boxLayout = new Box(BoxLayout.Y_AXIS);
        for (Component comp: listComponent)
            boxLayout.add(comp);

        boxLayout.add(labelPanel);
        boxLayout.add(comboBox);

        panel.removeAll();
        panel.add(boxLayout);
        panel.updateUI();
    }

    public class FileItemActionListener implements ActionListener{
        JMenuItem otherItem;
        public FileItemActionListener(JMenuItem item){
            otherItem = item;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser openFile = new JFileChooser();
            openFile.showOpenDialog(frame);

            File file = openFile.getSelectedFile();
            if (file != null){
                loadIdSubjectFile(file);
                idSubjectFile = file;
                otherItem.setEnabled(true);
            }
        }
    }
    public class SourceItemActionListen implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser openSource = new JFileChooser();
            openSource.showOpenDialog(frame);

            File source = openSource.getSelectedFile();
            if(source != null) {
                try {
                    loadExcelSourceFile(source);
                    sourceExcelFile = source;
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    public class CheckBoxActionListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            JCheckBox checkBox = (JCheckBox)(e.getSource());
            String name = checkBox.getText();

            if(checkBox.isSelected()) {
                hauToList.add(name);
            }
            else{
                hauToList.remove(name);
            }
        }
    }
    public class ComboBoxActionListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            JComboBox comboBox = (JComboBox)(e.getSource());
            int index = comboBox.getSelectedIndex();

            //quickSort(tkbList, 0, tkbList.size()-1, index-1);

            ArrayList<TKB> sorted = (ArrayList<TKB>) tkbList.clone();
            switch (index){
                case 0:
                    break;
                case 1:
                    sorted.sort(new SortByDayOff());
                    break;
                case 2:
                    sorted.sort(new SortByMorningBreak());
                    break;
                case 3:
                    sorted.sort(new SortByAfternoonBreak());
                    break;
                case 4:
                    sorted.sort(new SortByAllBreak());
                    break;
            }

            loadListTKB(southSrollListTKB, sorted);
        }
    }

    private void findValidTKB(){
        printInfo("Start find valid TKB");
        // Lấy danh sách các môn học
        ArrayList<ArrayList<Subject>> allSubjectList = new ArrayList<>();
        for(int i=0; i<idSubjectList.size(); ++i) {
            ArrayList<Subject> subjectArrayList = (ArrayList<Subject>) (sourceMap.get(idSubjectList.toArray()[i]));
            if (subjectArrayList.size() == 0){ // nếu có bất kig danh sách nào trống => không có lớp nào phù hợp với môn học tương ứng của nó
                printInfo("Cannot find TKB");
                return;
            }
            allSubjectList.add(subjectArrayList);
        }

        // khởi tạo chỉ số để lấý phần tử của các list
        int[] index = new int[allSubjectList.size()];
        for(int i=0; i< index.length; ++i)
            index[i] = 0;

        // Vét cạn hết tất cả các trường hợp
        ArrayList<Subject> subjectList = new ArrayList<>();
        while (true){
            for(int i= index.length - 1; i>=0; --i) {
                ArrayList<Subject> subjects = (ArrayList<Subject>)sourceMap.get(idSubjectList.toArray()[i]);
                if (index[i] == subjects.size()) { // kiểm tra chỉ số của list với list tương ứng với nó
                    if(i == 0 ){
                        printInfo("Done find valid TKB");
                        return;
                    }
                    index[i] = 0;
                    index[i - 1] += 1;
                }
            }
            // lấy phần tử của từng list với chỉ số tương ứng
            for(int i=0; i< index.length; ++i) {
                ArrayList<Subject> list = allSubjectList.get(i);
                subjectList.add(list.get(index[i]));
            }

            if(isValidSubjectList(subjectList)){
                String name = String.format("Sheet_%d", tkbList.size()+1);
                TKB tkb = new TKB(frame, name, (ArrayList<Subject>) subjectList.clone());
                tkbList.add(tkb);
            }

            index[index.length-1] += 1;
            subjectList.clear();
        }

    }
    private boolean isValidSubjectList(ArrayList<Subject> subjectList){
        for(int i = 0; i<subjectList.size(); ++i) {
            Subject subject1 = subjectList.get(i);
            for (int j = i+1; j < subjectList.size(); ++j) {
                Subject subject2 = subjectList.get(j);

                if(isCollapseSubject(subject1, subject2)) // collapse => khong the tao thoi khoa bieu
                    return false;
            }
        }
        return true;
    }
    boolean isCollapseSubject(Subject subject1, Subject subject2){ // neu 2 mang co 1 phan tu giong nhau => collapse

        ArrayList<Lesson> lessonsLTSubject1 = subject1.getLessonLT();
        ArrayList<Lesson> lessonsLTSubject2 = subject2.getLessonLT();

        ArrayList<Lesson> lessons = new ArrayList<Lesson>();
        lessons.addAll(subject1.getLessonLT());
        lessons.addAll(subject2.getLessonLT());

        if(subject1.getLessonTH() != null)
            lessons.add(subject1.getLessonTH().get(0));
        if(subject2.getLessonTH() != null)
            lessons.add(subject2.getLessonTH().get(0));

        if(isCollapseLesson(lessons))
            return true;
        return false;
    }
    boolean isCollapseLesson(ArrayList<Lesson> lessons){

        for(int i=0; i< lessons.size() - 1; ++i)
            for (int j=i+1; j< lessons.size(); ++j){
                Lesson lesson1 = lessons.get(i);
                Lesson lesson2 = lessons.get(j);
                if(lesson1.getDateOfWeek() == lesson2.getDateOfWeek()) {
                    int[] time1 = lesson1.getTime();
                    int[] time2 = lesson2.getTime();

                    ArrayList<Integer> time2List = new ArrayList<Integer>();

                    for(int t : time2)
                        time2List.add(t);

                    for(int t1 : time1)
                        if(time2List.contains(t1))
                            return true;
                }
            }

        return false;
    }

    private void printInfo(String status){
        infoArea.setText(infoArea.getText() + '\n' + status);
    }
    private void loadListTKB(JScrollPane scroll){
        // với mỗi phần tử trong tkbList => tạo 1 button tương ứng
        JPanel temp = new JPanel();
        for (TKB tkb : tkbList){
            JButton button = new JButton(tkb.nameTKB);
            temp.add(button);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    centerPanel.removeAll();

                    centerPanel.add(tkb);
                    centerPanel.add(loadInfoTKBPanel(tkb));
                    centerPanel.updateUI();
                }
            });
        }


        scroll.setViewportView(temp);
        scroll.updateUI();

    }

    private void loadListTKB(JScrollPane scroll, ArrayList<TKB> list){
        // với mỗi phần tử trong tkbList => tạo 1 button tương ứng
        JPanel temp = new JPanel();
        for (TKB tkb : list){
            JButton button = new JButton(tkb.nameTKB);
            temp.add(button);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    centerPanel.removeAll();

                    centerPanel.add(tkb);
                    centerPanel.add(loadInfoTKBPanel(tkb));
                    centerPanel.updateUI();
                }
            });
        }


        scroll.setViewportView(temp);
        scroll.updateUI();

    }

    public class SortByDayOff implements Comparator<TKB>{

        @Override
        public int compare(TKB o1, TKB o2) {
            Integer s1 = o1.getSumBreakAllday();
            Integer s2 = o2.getSumBreakAllday();

            return s2.compareTo(s1);
        }
    }

    public class SortByMorningBreak implements Comparator<TKB>{
        @Override
        public int compare(TKB o1, TKB o2) {
            Integer s1 = o1.getSumBreakMorning();
            Integer s2 = o2.getSumBreakMorning();

            return s2.compareTo(s1);
        }
    }
    public class SortByAfternoonBreak implements Comparator<TKB>{

        @Override
        public int compare(TKB o1, TKB o2) {
            Integer s1 = o1.getSumBreakAfternoon();
            Integer s2 = o2.getSumBreakAfternoon();

            return s2.compareTo(s1);
        }
    }
    public class SortByAllBreak implements Comparator<TKB>{

        @Override
        public int compare(TKB o1, TKB o2) {
            Integer s1 = o1.getSumBreak();
            Integer s2 = o2.getSumBreak();

            return s2.compareTo(s1);
        }
    }

/*    void quickSort(ArrayList<TKB> data, int l, int r, int case_){
        // Xắp xếp giảm dần
        // If the first index less or equal than the last index
        if (l <= r) {
            // Create a Key/Pivot Element
            TKB key = data.get((l+r)/2);

            // Create temp Variables to loop through array
            int i = l;
            int j = r;

            while (i <= j)
            {
                int keyValue = getSumBreakLessoon(key, case_);

                int itemValue = getSumBreakLessoon(tkbList.get(i), case_);
                while (itemValue > keyValue) {
                    i++;
                    itemValue = getSumBreakLessoon(tkbList.get(i), case_);
                }

                itemValue = getSumBreakLessoon(tkbList.get(j), case_);
                while (itemValue < keyValue) {
                    j--;
                    itemValue = getSumBreakLessoon(tkbList.get(j), case_);
                }

                if (i <= j)
                {
                    Collections.swap(data, i, j);
                    i++;
                    j--;
                }
            }

            // Recursion to the smaller partition in the array after sorted above

            if (l < j)
                quickSort(data, l, j, case_);
            if (r > i)
                quickSort(data, i, r, case_);
        }
    }
    
    private int getSumBreakLessoon(TKB tkb, int key){
        switch (key){
            case TKB.SUM_BREAK_ALLDAY:
                return tkb.getSumBreakAllday();

            case TKB.SUM_BREAK_MORNING:
                return tkb.getSumBreakMorning();

            case TKB.SUM_BREAK_AFTERNOON:
                return tkb.getSumBreakAfternoon();

            case TKB.SUM_BREAK:
                return tkb.getSumBreak();

            default:
                throw new IllegalStateException("Unexpected value: " + key);
        }
    }*/
}
