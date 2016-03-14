package main;

import junit.framework.Assert;

import java.io.IOException;

public class MoodlerTest {

    private final String username = "kaspar_p";
    private final String correctPassword = "5202436kapu";
    private final String incorrectPassword = "adobe123";

/*
    public static void main(String[] args) throws IOException {
        Moodler moodler = new Moodler("kaspar_p", "5202436kapu");
        System.out.println("Login successful: " + moodler.login());
        Course course = moodler.courseByName("disk");
        if(course == null) {
            System.out.println("Was unable to find requested course.");
        } else {
            System.out.println("Found course: " + course.getName());
            System.out.println("Final grade of this course is " + moodler.finalGrade(course).getGrade());
        }
    }
*/


    @org.junit.Before
    public void setUp() throws Exception {

    }

    @org.junit.Test
    public void testSuccessfulLoginReturnsTrue() throws Exception {
        Moodler moodler = new Moodler(username, correctPassword);
        boolean loginSuccessful = moodler.login();

        Assert.assertTrue(loginSuccessful);
    }

    @org.junit.Test
    public void testUnsuccessfulLoginReturnsFalse() throws Exception {
        Moodler moodler = new Moodler(username, incorrectPassword);
        boolean loginSuccessful = moodler.login();

        Assert.assertFalse(loginSuccessful);
    }

    @org.junit.Test
    public void testAccessAfterSuccessfulLogin() throws Exception {
        Moodler moodler = getInstanceLoggedIn(username, correctPassword);
        String html = moodler.getHTMLFromURLPublic("https://moodle.ut.ee/my/");

        Assert.assertTrue(html.contains("Tere tulemast"));
    }

    @org.junit.Test
    public void testAccessAfterUnsuccessfulLogin() throws Exception {
        Moodler moodler = getInstanceLoggedIn(username, incorrectPassword);
        String html = moodler.getHTMLFromURLPublic("https://moodle.ut.ee/my/");

        Assert.assertFalse(html.contains("Tere tulemast"));
    }

    @org.junit.Test
    public void testRemainLoggedInAfterMultipleRequests() throws Exception {
        Moodler moodler = getInstanceLoggedIn(username, correctPassword);
        for (int i = 0; i < 3; i++) {
            Assert.assertTrue(moodler.getHTMLFromURLPublic("https://moodle.ut.ee/my/").contains("Tere tulemast"));
        }
    }

    @org.junit.Test
    public void testRemainLoggedInAfterMultipleLogins() throws Exception {
        Moodler moodler = new Moodler(username, correctPassword);
        for (int i = 0; i < 3; i++) {
            moodler.login();
            Assert.assertTrue(moodler.getHTMLFromURLPublic("https://moodle.ut.ee/my/").contains("Tere tulemast"));
        }
    }

    @org.junit.Test
    public void testFinalGrade() throws Exception {

    }

    @org.junit.Test
    public void testCourseByNamePartialName() throws Exception {
        Moodler moodler = getInstanceLoggedIn(username, correctPassword);
        Course course = moodler.courseByName("algoritmid");

        Assert.assertEquals(new Course("182", "Algoritmid ja andmestruktuurid (MTAT.03.133)"), course);
    }

    @org.junit.Test
    public void testCourseByNameFullName() throws Exception {
        Moodler moodler = getInstanceLoggedIn(username, correctPassword);
        Course course = moodler.courseByName("Algoritmid ja andmestruktuurid (MTAT.03.133)");

        Assert.assertEquals(new Course("182", "Algoritmid ja andmestruktuurid (MTAT.03.133)"), course);
    }


    @org.junit.Test
    public void testCourseByNameNonexistant() throws Exception {
        Moodler moodler = getInstanceLoggedIn(username, correctPassword);
        Course course = moodler.courseByName("Võrdõiguslikkus arvutiteaduses");

        Assert.assertNull(course);
    }

    @org.junit.Test
    public void testCourseByIdSuccessful() throws Exception {
        Moodler moodler = getInstanceLoggedIn(username, correctPassword);
        Course course = moodler.courseById("182");

        Assert.assertEquals(new Course("182", "Algoritmid ja andmestruktuurid (MTAT.03.133)"), course);
    }

    @org.junit.Test
    public void testCourseByIdUnsuccessful() throws Exception {
        Moodler moodler = getInstanceLoggedIn(username, correctPassword);
        Course course = moodler.courseById("1337");

        Assert.assertNull(course);
    }





    /*
        Convenience methods
     */

    private Moodler getInstanceLoggedIn(String username, String password) throws IOException {
        Moodler moodler = new Moodler(username, password);
        moodler.login();
        return moodler;
    }
}