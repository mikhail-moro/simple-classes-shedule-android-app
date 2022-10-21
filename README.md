# simple-classes-shedule-android-app

### Simple android app that can display user month and daily schedule of classes


<sub>Month schedule</sub>

[<img src="https://github.com/mikhail-moro/res/blob/main/month_schedule.jpg" width="240" />](https://github.com/mikhail-moro/res/blob/main/month_schedule.jpg)

<sub>Month schedule with list of daily classes</sub>

[<img src="https://github.com/mikhail-moro/res/blob/main/month_schedule_with_raised_bottom_list.jpg" width="240" />](https://github.com/mikhail-moro/res/blob/main/month_schedule_with_raised_bottom_list.jpg)

<sub>Daily schedule</sub>

[<img src="https://github.com/mikhail-moro/res/blob/main/daily_schedule.jpg" width="240" />](https://github.com/mikhail-moro/res/blob/main/daily_schedule.jpg)


User can change month of the month schedule and day of the daily schedule with right\left swipe.


## Backend

In this moment app uses domein http://mikhailmoro.pythonanywhere.com/ to get data of classes, but it can be changed in 
simple-classes-shedule-android-app/app/src/main/java/com/example/lessonsschedule/Request.kt
Kotlin
```
  private const val SERVER_URL = "https://mikhailmoro.pythonanywhere.com/"
```

But body of response shoud have this structure:
JS
```
{
  ...
  
  "yyyy-mm-dd": [
    ...,
    
    {
      "module": ...,
      "name": " ...,
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
