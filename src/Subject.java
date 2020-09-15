import java.util.ArrayList;

public class Subject {
    String idClass;
    String name;
    String lecturer;
    String lecturer_lt;
    String lecturer_th;
    int tc_lt = 0; // tín chỉ lý thuyết
    int tc_th = 0; // tín chỉ thực hành

    ArrayList<Lesson> lessonLT;
    ArrayList<Lesson> lessonTH;

    public Subject(){
    }
    public Subject(String idClass_, String name_, String lecturer_,
                   Lesson lesson_){
        idClass = idClass_;
        name = name_;
        lecturer = lecturer_;
        addLessonLT(lesson_);

    }

    public Subject(String idClass_, String name_, String lecturer_lt, String lecturer_th,
                   Lesson lesson_){
        idClass = idClass_;
        name = name_;
        this.lecturer_lt = lecturer_lt;
        this.lecturer_th = lecturer_th;
        addLessonLT(lesson_);
    }

    public Subject(String idClass_, String name_){
        idClass=idClass_;
        name = name_;
    }

    public  String getIdClass(){return idClass;}
    /*String getName(){return name;}
    String getLecturer(){return lecturer;}*/
    public void addLessonLT(Lesson lesson){
        if(lessonLT == null)
            lessonLT = new ArrayList<Lesson>();
        lessonLT.add(lesson);
    }
    public void addLessonTH(Lesson lesson){
        if(lessonTH == null)
            lessonTH = new ArrayList<Lesson>();
        lessonTH.add(lesson);
    }

    public void setLecturer_lt(String lt){lecturer_lt = lt;}
    public void setLecturer_th(String th){lecturer_th = th;}

    public void setTc_lt(int soTC){
        tc_lt = soTC;
    }
    public void setTc_th(int soTC){
        tc_th = soTC;
    }

    public ArrayList<Lesson> getLessonLT(){ return lessonLT; }
    public ArrayList<Lesson> getLessonTH(){ return lessonTH; }

    public int getTc_lt(){return tc_lt;}
    public int getTc_th(){return  tc_th;}
}
