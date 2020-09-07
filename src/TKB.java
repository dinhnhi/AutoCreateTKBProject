import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class TKB extends JPanel {
    ArrayList<Subject> listSubject;
    String nameTKB;
    AttributiveCellTableModel ml;
    JFrame parent;
    boolean[][] presentTKB;

    public static void main(String[] args){
    }

    public TKB(JFrame parent_, String name_, ArrayList<Subject> list_){
        this(parent_);
        nameTKB = name_;
        listSubject = list_;

        loadPresentTKB();
        drawTable();
    }

    public TKB(JFrame parent_){
        String[] columnName = new String[]{"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7"};
        ml = new AttributiveCellTableModel(new Object[11][6], columnName);

        MultiSpanCellTable table = new MultiSpanCellTable(ml);
        table.setRowHeight(25);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(parent_.getWidth() - 100, parent_.getHeight()));

        add(scroll);
    }

    public void drawTable(){
        CellSpan cellAt = (CellSpan)ml.getCellAttribute();

        // merge dòng thứ 6 để
        // phan cach buoi 12345, 67890
        cellAt.combine(new int[]{5}, new int[]{0, 1, 2, 3, 4, 5});

        for(int i=0; i<listSubject.size(); ++i){
            Subject subject = listSubject.get(i);
            ArrayList<Lesson> lessonLT = (ArrayList<Lesson>) subject.getLessonLT().clone();

            drawLesson(subject.idClass, lessonLT);

            if(subject.getLessonTH() != null) { // tat ca lop thuc hanh cua 1 mon thi cung tiet => chi ve 1 cai
                ArrayList<Lesson> lessonTH = (ArrayList<Lesson>) subject.getLessonTH().clone();
                if (lessonTH.size() == 1)
                    drawLesson(subject.idClass + ".1", lessonTH.get(0));
                else if (lessonTH.size() == 2)
                    drawLesson(subject.idClass + ".1/ .2", lessonTH.get(0));
            }
        }
    }

    private void loadPresentTKB(){
        presentTKB = new boolean[2][6]; // 2 => dai dien cho 2 buoi sang chieu, 6=> 6 ngay trong tuan

        for(Subject subject : listSubject){
            ArrayList<Lesson> lyThuyet = subject.getLessonLT();
            ArrayList<Lesson> thucHanh = subject.getLessonTH();

            if(lyThuyet.size() != 0)
                for(Lesson lesson : lyThuyet) {
                    int dateOfWeek = lesson.getDateOfWeek();
                    int[] time = lesson.getTime();

                    if(time[0] < 5)
                        presentTKB[0][dateOfWeek-2] = true;
                    else presentTKB[1][dateOfWeek-2] = true;
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

    public ArrayList<String> getListClass(){
        ArrayList<String> listClass = new ArrayList<String>();

        for(int i=0; i<listSubject.size(); ++i){
            Subject subject = listSubject.get(i);

            listClass.add(subject.getIdClass());

            if(subject.getLessonTH() != null) { // tat ca lop thuc hanh cua 1 mon thi cung tiet => chi ve 1 cai
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

    private void drawLesson(String name, ArrayList<Lesson> lessonArray) {
        CellSpan cellAt = (CellSpan)ml.getCellAttribute();

        for (int i = 0; i < lessonArray.size(); ++i) {
            Lesson lesson = lessonArray.get(i);
            drawLesson(name, lesson);
        }
    }
    private void drawLesson(String name, Lesson lesson) {
        CellSpan cellAt = (CellSpan)ml.getCellAttribute();

            int column = lesson.getDateOfWeek() - 2; // trừ 2 => vì dafeOfweek bắt đầu từ số 2
            int[] rows = lesson.getTime().clone();

            for (int j = 0; j < rows.length; ++j) {
                int value = rows[j];
                rows[j] = (value > 0 && value < 6) ? value - 1 : value;
            }

            ml.setValueAt(name, rows[0], column);
            cellAt.combine(rows, new int[]{column});
    }

    static final int SUM_BREAK_ALLDAY = 0;
    static final int SUM_BREAK_MORNING = 1;
    static final int SUM_BREAK_AFTERNOON = 2;
    static final int SUM_BREAK = 3;
}
