import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;

public class TKB extends JPanel {
    ArrayList<Subject> listSubject;
    String nameTKB;
    AttributiveCellTableModel ml;
    boolean[][] presentTKB; // Được dùng cho mục đích xác định các buổi học và buổi trống của TKB
    JTable otherInfoTable;

    public static void main(String[] args){
    }

    public TKB(String name_, ArrayList<Subject> list_){
        this();
        nameTKB = name_;
        listSubject = list_;

        loadPresentTKB();
        drawTable();
    }

    public TKB(){
        this.setLayout(new GridLayout(2, 1));

        DefaultTableModel tableModel = new DefaultTableModel();
        otherInfoTable = new JTable(tableModel);
        otherInfoTable.getTableHeader().setFont(new Font("arial", Font.BOLD, 13));
        tableModel.addColumn("Danh sách các môn học không có lịch học chính thức");
        otherInfoTable.setRowHeight(100);

        String[] columnName = new String[]{"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7"};
        ml = new AttributiveCellTableModel(new Object[11][6], columnName);
        MultiSpanCellTable table = new MultiSpanCellTable(ml);
        JTable jtable = (JTable)table;
        jtable.getTableHeader().setFont(new Font("arial", Font.BOLD, 13));
        jtable.setRowHeight(40);

        JScrollPane scrollTable = new JScrollPane(jtable);
        int hightHeader = jtable.getTableHeader().getPreferredSize().height;
        scrollTable.setPreferredSize(new Dimension(5*210,
                jtable.getRowHeight()*11 + hightHeader + 14)); // số 14 ở đây là cộng vô thêm cho nó table vừa với scroll

        // Căn giữa nội dung trong các cell của bảng
        DefaultTableCellRenderer centerRenderer= new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        centerRenderer.setVerticalAlignment(SwingConstants.CENTER);
        for(int i=0; i < jtable.getColumnCount(); ++i)
            jtable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        for(int i=0; i<otherInfoTable.getColumnCount(); ++i)
            otherInfoTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);

        JScrollPane scrollInfoTable = new JScrollPane(otherInfoTable);
        scrollInfoTable.setPreferredSize(new Dimension(500, 300));
        JPanel tempOtherInfoPanel = new JPanel();
        tempOtherInfoPanel.add(scrollInfoTable);

        add(scrollTable);
        add(tempOtherInfoPanel);
    }

    public void drawTable(){
        CellSpan cellAt = (CellSpan)ml.getCellAttribute();

        // merge dòng thứ 6 để
        // phan cach buoi 12345, 67890
        cellAt.combine(new int[]{5}, new int[]{0, 1, 2, 3, 4, 5});

        for(int i=0; i<listSubject.size(); ++i){
            Subject subject = listSubject.get(i);
            ArrayList<Lesson> lessonLT = (ArrayList<Lesson>) subject.getLessonLT().clone();

            String[] dataLT = {subject.idClass, subject.name, subject.lecturer};
            drawLesson(dataLT, lessonLT);

            String[] dataTH = {subject.idClass, subject.name, subject.lecturer_th};
            if(subject.getLessonTH() != null) { // tat ca lop thuc hanh cua 1 mon thi cung tiet => chi ve 1 cai
                ArrayList<Lesson> lessonTH = (ArrayList<Lesson>) subject.getLessonTH().clone();
                if (lessonTH.size() == 1)
                    dataTH[0] += ".1";
                else if (lessonTH.size() == 2)
                    dataTH[0] += ".1/ .2";

                drawLesson(dataTH, subject.lessonTH);
            }
        }
    }

    private void loadPresentTKB(){
        presentTKB = new boolean[2][6]; // 2 => dai dien cho 2 buoi sang chieu, 6=> 6 ngay trong tuan

        for(Subject subject : listSubject){
            ArrayList<Lesson> lyThuyet = subject.getLessonLT();
            ArrayList<Lesson> thucHanh = subject.getLessonTH();

            if(lyThuyet !=null && lyThuyet.size() != 0)
                for(Lesson lesson : lyThuyet) {
                    int dateOfWeek = lesson.getDateOfWeek();
                    int[] time = lesson.getTime();

                    if(dateOfWeek == -1)
                        continue;

                    if(time[0] < 5)
                        presentTKB[0][dateOfWeek-2] = true; // có tiết vào buổi sáng
                    else presentTKB[1][dateOfWeek-2] = true; // có tiết cào buổi chiều
                }

            if(thucHanh != null && thucHanh.size() != 0){
                for(Lesson lesson : thucHanh) {
                    int dateOfWeek = lesson.getDateOfWeek();
                    int[] time = lesson.getTime();

                    if(dateOfWeek == -1)
                        continue;

                    if(time[0] < 5)
                        presentTKB[0][dateOfWeek-2] = true;
                    else presentTKB[1][dateOfWeek-2] = true;
                }
            }
        }
    }

    public int getSumBreakMorning(){
        int sum = 0;
        for(int i =0; i<6; ++i)
            if(!presentTKB[0][i])
                sum += 1;

        return sum;
    }
    public int getSumBreakAfternoon(){
        int sum = 0;
        for(int i =0; i<6; ++i)
            if(!presentTKB[1][i])
                sum += 1;

        return sum;
    }
    public int getSumBreak(){
        int sum = 0;
        for(int i =0; i<6; ++i) {
            if (!presentTKB[0][i])
                sum += 1;
            if(!presentTKB[1][i])
                sum += 1;
        }
        return sum;
    }
    public int getSumBreakAllday(){
        int sum = 0;
        for(int i =0; i<6; ++i)
            if(!presentTKB[0][i] && !presentTKB[1][i])
                sum += 1;

        return sum;
    }

    public ArrayList<String> getListClass(){ // Lấy danh sách mã lớp của các môn trong tkb => để cho người dùng đkí h.phần
        ArrayList<String> listClass = new ArrayList<String>();

        for(int i=0; i<listSubject.size(); ++i){
            Subject subject = listSubject.get(i);

            listClass.add(subject.getIdClass());

            if(subject.getLessonTH() != null) { // tat ca lop thuc hanh cua 1 mon thì cung tiet => chi ve 1 cai
                ArrayList<Lesson> lessonTH = (ArrayList<Lesson>) subject.getLessonTH().clone();
                if (lessonTH.size() == 1)
                    listClass.add(subject.getIdClass() + ".1");
                else if (lessonTH.size() == 2)
                    listClass.add(subject.getIdClass() + ".1/ .2");
            }
        }

        return listClass;
    }
    public int getSoTC(){
        int soTC = 0;

        for (Subject s : listSubject)
            soTC += (s.getTc_lt() + s.getTc_th());

        return soTC;
    }

    private void drawLesson(String[] name, ArrayList<Lesson> lessonArray) {

        for (int i = 0; i < lessonArray.size(); ++i) {
            Lesson lesson = lessonArray.get(i);
            drawLesson(name, lesson);
        }
    }
    private void drawLesson(String[] name, Lesson lesson) {
        CellSpan cellAt = (CellSpan)ml.getCellAttribute();

            int column = lesson.getDateOfWeek() - 2; // trừ 2 => vì dafeOfweek bắt đầu từ số 2
            int[] rows = lesson.getTime().clone();

            if(lesson.getDateOfWeek() == -1){
                String html = toHtml(name);
                addInfo(otherInfoTable, html);
                return;
            }

            for (int j = 0; j < rows.length; ++j) {
                int value = rows[j];
                rows[j] = (value > 0 && value < 6) ? value - 1 : value;
            }

            String html = toHtml(name);
            ml.setValueAt(html, rows[0], column);
            cellAt.combine(rows, new int[]{column});
    }

    private void addInfo(JTable form, String str){
        DefaultTableModel tableModel = (DefaultTableModel)form.getModel();
        tableModel.addRow(new Object[]{str});
    }

    private String toHtml(String[] list){
        String data = String.join("<br>", list);
        String html = "<html>"+
                "<style>" +
                "body {font-family: Arial, sans-serif;" +
                "font-size: 13px;" +
                "text-align: center;}" +
                "</style>" +
                "<body>" +
                data +
                "</body>" +
                "</html>";
        return html;
    }

    static final int SUM_BREAK_ALLDAY = 0;
    static final int SUM_BREAK_MORNING = 1;
    static final int SUM_BREAK_AFTERNOON = 2;
    static final int SUM_BREAK = 3;
}
