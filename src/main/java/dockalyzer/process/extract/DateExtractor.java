package dockalyzer.process.extract;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by salizumberi-laptop on 30.10.2016.
 */
public class DateExtractor {

    private final static ArrayList<Integer> months = new ArrayList<>();

    private static void classfiyDatesToMonths(ArrayList<Date> commitDates){
        for(int i=0; i<commitDates.size(); i++){
            addToMonth(getMonth(commitDates.get(i)));
        }
    }

    public static void addToMonth(int month){
        for (int i = 0; i < 12; i++) {
            if (i==month-1){
                months.set(i,months.get(i)+1);
            }
        }
    }

    public static void createMonths(){
        Integer month = new Integer(0);
        for (int i = 0; i < 12; i++) {
            months.add(month);
        }
    }

    public static int getMonth(java.util.Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.MONTH)+1;
    }
}
