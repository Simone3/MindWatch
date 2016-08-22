package it.polimi.aui.auiapp;

public class AlarmReceiverTest
{
    /*AlarmReceiver alarmReceiver = new AlarmReceiver();
    Calendar calendar = GregorianCalendar.getInstance();
    Date[] dates;
    int alarmsToSchedule;
    int startHour;
    int endHour;

    @Test
    public void computeAlarmTimesTest()
    {
        alarmsToSchedule = 10;
        startHour = 8;
        endHour = 20;

        dates = alarmReceiver.computeAlarmTimes(alarmsToSchedule, startHour, endHour);
        testDates(true);

        alarmsToSchedule = 5;

        dates = alarmReceiver.computeAlarmTimes(alarmsToSchedule, startHour, endHour);
        testDates(true);

        alarmsToSchedule = 20;

        dates = alarmReceiver.computeAlarmTimes(alarmsToSchedule, startHour, endHour);
        testDates(true);

        alarmsToSchedule = 5;
        startHour = 12;
        endHour = 20;

        dates = alarmReceiver.computeAlarmTimes(alarmsToSchedule, startHour, endHour);
        testDates(true);

        alarmsToSchedule = 20;
        startHour = 12;
        endHour = 20;

        dates = alarmReceiver.computeAlarmTimes(alarmsToSchedule, startHour, endHour);
        testDates(false);
    }

    private void testDates(boolean shouldScheduleAllAlarms)
    {
        assertNotNull(dates);

        System.out.println("DATES FOR " + alarmsToSchedule + "-" + startHour + "-" + endHour + " ARE "+dates.length+": ");

        if(shouldScheduleAllAlarms) assertEquals(alarmsToSchedule, dates.length);
        else assertTrue(alarmsToSchedule>dates.length);
        for(Date date: dates)
        {
            calendar.setTime(date);

            System.out.println("--- " + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE));

            assertTrue(calendar.get(Calendar.HOUR_OF_DAY) >= startHour);
            assertTrue(calendar.get(Calendar.HOUR_OF_DAY) < endHour);
        }
    }*/
}
