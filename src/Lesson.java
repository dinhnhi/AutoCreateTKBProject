

public class Lesson {
    private int dateOfWeek;
    private int[] time;

    public Lesson(){}
    public Lesson(int dateOfWeek_, int[] time_){
        dateOfWeek = dateOfWeek_;
        time = time_;
    }
    public Lesson(String dateOfWeek_, String time_){
        dateOfWeek = Integer.parseInt(dateOfWeek_);

        char[] list = time_.toCharArray();
        time = new int[list.length];
        for(int i=0; i< list.length; ++i) {
            time[i] = Integer.parseInt(String.valueOf(list[i]));

            if(time[i] == 0) time[i] = 10;
        }
    }

    void setDateOfWeek(int date){dateOfWeek = date;}
    void setTime(int[] time_){time = time_;}

    int getDateOfWeek(){return dateOfWeek;}
    int[] getTime(){return time;}
}
