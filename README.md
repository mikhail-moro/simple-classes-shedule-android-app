# simple-classes-shedule-android-app

### Simple android app that can display to user month and day schedules of classes


<sub>Month schedule</sub>

[<img src="https://github.com/mikhail-moro/res/blob/main/month_schedule.jpg" width="240" />](https://github.com/mikhail-moro/res/blob/main/month_schedule.jpg)

<sub>Month schedule with list of daily classes</sub>

[<img src="https://github.com/mikhail-moro/res/blob/main/month_schedule_with_raised_bottom_list.jpg" width="240" />](https://github.com/mikhail-moro/res/blob/main/month_schedule_with_raised_bottom_list.jpg)

<sub>Day schedule</sub>

[<img src="https://github.com/mikhail-moro/res/blob/main/daily_schedule.jpg" width="240" />](https://github.com/mikhail-moro/res/blob/main/daily_schedule.jpg)


User can change month of the month schedule and day of the day schedule with right\left swipe and summon list of day classes if tap on a cell in month schedule.


## Backend

In this moment app uses domein http://mikhailmoro.pythonanywhere.com/ to get data of classes, but it can be changed in 
simple-classes-shedule-android-app/app/src/main/java/com/example/lessonsschedule/Request.kt
```Kotlin
  class Request {
      companion object {
          private const val SERVER_URL = "https://mikhailmoro.pythonanywhere.com/"
      }
      
      ...
  }
```

**But without the code changing, .json in body of response shoud have this structure:**
```
{
    ...
  
        "yyyy-mm-dd": [
            ...,
    
            {
                "module": ...,
                "name": ...,
                "theme": ...,
                "type": ...,
                "aud": ...,
                "link": ...,
                "teachers": ...,
                "groups": ...,
                "startTime": ...,
                "endTime": ...,
                "color": ...,
            },
    
            ...
        ]
  
    ...
}
```

## In the next builds:
  + At the moment, many texts and colors are hardcoded, this is incorrect and it's will be changed in next builds
  + Loading animations will be added
  + Preferences (as theme changing) will be added
  + App icon wiil be changed
  + Login activity will be added 
