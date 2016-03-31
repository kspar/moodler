# moodler

**moodler** is an open source [UT Moodle](https://moodle.ut.ee/my/) API for Java.

## Installation
*Coming soon!*

## Examples
Find a course by name and fetch its final grade. This assumes that the logged in user is enrolled in the course *Algoritmid ja andmestruktuurid*.
```Java
Moodler m = new Moodler("myuser", "mypass");
m.login();
Course aa = m.courseByName("algoritmid ja andmestruktuurid");
Grade aaFinal = m.finalGrade(aa);
System.out.println(String.format("Course '%s' final grade: %s", aa.getName(), aaFinal.getGrade()));
```



