package main;

import exceptions.NoCredentialsProvidedException;
import exceptions.NoInternetConnectionException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.impl.client.BasicCookieStore;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class Moodler {

    private final String authURL = "https://moodle.ut.ee/login/index.php";
    private final String gradesRawURL = "https://moodle.ut.ee/grade/report/user/index.php?id=";
    private final String mainPageURL = "https://moodle.ut.ee/my/";

    private final CookieStore cookieStore;
    private final Executor executor;

    private String username;
    private String password;
    private boolean performedLogin;

    // TODO: should take into consideration the expiration of login.
    // Main problem is how to check if login token has expired.
    // One solution would be to try a request and determine by the output (best).
    // Another one would be to remember the last time a successful login or request was made and calculate
    // based on that and some fixed 'timeout' whether the login token has expired (easier).


    // TODO: should provide a method for searching for multiple courses?

    public Moodler(String username, String password) {
        this.username = username;
        this.password = password;
        this.performedLogin = false;

        // Initialize cookie management and request execution systems
        this.cookieStore = new BasicCookieStore();
        this.executor = Executor.newInstance();
        executor.use(cookieStore);
    }

    public static void main(String[] args) throws IOException {
        Moodler moodler = new Moodler("kaspar_p", "5202436kapu");
        moodler.login();
        System.out.println(moodler.courseByName("algoritmid"));
    }

    public boolean login() throws IOException {
        // TODO: should (also) provide a method that reveals reason for failed login?
        // TODO: should ask login details in 'login()' or in constructor? Probs in 'login()'
        Login login = loginInternal();
        return login.isSuccessful();
    }

    /**
     * Fetch the final grade of a course
     *
     * @param course
     * @return <code>Grade</code> object representing the final grade
     * @throws IOException
     */
    public Grade finalGrade(Course course) throws IOException {

        if (course == null) {
            throw new IllegalArgumentException("Course can't be null!");
        }

        String html = fetchGetContent(gradesRawURL + course.getId());
        Document doc = Jsoup.parse(html);

        // Select td elements that (contain an attribute 'header' that contains value 'grade')
        // and (are from a class 'oddd1'/'evend1')
        Elements oddGradeElements = doc.select("td[headers*=grade].oddd1");
        Elements evenGradeElements = doc.select("td[headers*=grade].evend1");

        // TODO: Can the final grade also be from class 'evend1'? No examples of this yet found.
        if (evenGradeElements.size() != 0) {
            throw new AssertionError("Expected no even grade elements but found " + evenGradeElements.size());
        }

        if (oddGradeElements.size() != 1) {
            throw new RuntimeException("Expected to find 1 odd grade element but found " + oddGradeElements.size());
        }

        String finalGrade = oddGradeElements.first().text();

        return new Grade(finalGrade);
    }

    /**
     * Find a course that <b>contains</b> the given course name. Searches only among courses
     * the logged-in user has enrolled for.
     * <p>
     * Returns null if no matching course if found.
     * Returns null if more than 1 matching courses are found.
     * </p>
     * <p>
     * Example:<br>
     * <code>courseByName("algoritmid")</code> returns<br>
     * <code>Course{name='Algoritmid ja andmestruktuurid (MTAT.03.133)'}</code><br>
     * if the logged-in user has enrolled for that course and has not enrolled for any other
     * courses whose names contain the substring "algoritmid".
     * </p>
     *
     * @param courseName a substring of the course name to be found
     * @return <code>Course</code> object representing the found course if
     * exactly 1 course matched the given name, null otherwise
     * @throws IOException
     */
    public Course courseByName(String courseName) throws IOException {

        if (courseName == null) {
            throw new IllegalArgumentException("Course name can't be null!");
        } else if (courseName.isEmpty()) {
            throw new IllegalArgumentException("Course name can't be empty!");
        }

        String html = fetchGetContent(mainPageURL);
        Document doc = Jsoup.parse(html);

        String requestedCourseId = "";
        String requestedCourseName = "";
        int matchingCourses = 0;
        for (Element el : doc.select("h3 > a")) {

            if (el.text().isEmpty()) {
                // Wrong element
                continue;
            }

            List<Parameter> courseLinkParams = extractParamsFromURL(el.attr("href"));
            if (courseLinkParams.size() != 1) {
                throw new RuntimeException("Expected course URL to contain 1 query parameter, but found " +
                        courseLinkParams.size() + ": " + el.attr("href"));
            } else if (!courseLinkParams.get(0).getName().equals("id")) {
                throw new RuntimeException("Expected course URL to contain query parameter 'id', but found parameter '" +
                        courseLinkParams.get(0).getName() + "' :" + el.attr("href"));
            }

            String courseId = courseLinkParams.get(0).getValue(); // Value of course URL query parameter 'id' == course ID

            // Check match
            if (el.text().toLowerCase().contains(courseName.toLowerCase())) {
                matchingCourses++;
                requestedCourseId = courseId;
                requestedCourseName = el.text();
            }
        }

        if (matchingCourses == 1) {
            return new Course(requestedCourseId, requestedCourseName);
        } else {
            return null;
        }
    }


    public Course courseById(String courseID) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Extracts URL query parameters from a given string and returns
     * them as <code>Parameter</code> objects.
     * <p>
     * Example:
     * <code>"https://moodle.ut.ee/grade/report/user/index.php?id=182" -> [Parameter{'id=182'}]</code>
     *
     * @param url whose parameters are to be extracted
     * @return a list of <code>Parameter</code> objects, each representing
     * one URL query parameter
     */
    private List<Parameter> extractParamsFromURL(String url) {
        String paramString = url.substring(url.indexOf('?') + 1);

        List<Parameter> paramList = new ArrayList<>();

        for (String param : paramString.split("&")) {
            paramList.add(new Parameter(param));
        }

        return paramList;
    }

    /**
     * Fetches HTTP GET request content from a given URI
     *
     * @param uri of the resource whose content is to be fetched
     * @return content as a String
     * @throws IOException
     */
    private String fetchGetContent(String uri) throws IOException {
        Request request = Request.Get(uri);
        Response response = executor.execute(request);
        return response.returnContent().asString();
    }

    /**
     * FOR TESTING PURPOSES ONLY
     */
    public String getHTMLFromURLPublic(String url) throws IOException {
        Request request = Request.Get(url);
        Response response = executor.execute(request);
        return response.returnContent().asString();
    }

    /**
     * Attempts a login to Moodle with the username and password previously provided. Returns
     * <code>Login</code> object representing the attempt.
     *
     * @return a <code>Login</code> object representing the login attempt
     * @throws IOException
     */
    private Login loginInternal() throws IOException {
        if (username == null || password == null) {
            throw new NoCredentialsProvidedException("Username and password must be provided before attempting login.");
        }

        // TODO: should we perform internet connection checks like this? Should we perform it in every public 'fetch'-type method?
        if (!isReachable(new URL(authURL))) {
            throw new NoInternetConnectionException();
        }

        Request authRequest = Request.Post(authURL).bodyForm(
                Form.form().add("username", username).add("password", password).build());
        Response authResponse = executor.execute(authRequest);

        // Will throw HttpResponseException if request was unsuccessful
        return parseAuthResponse(authResponse);
    }

    /**
     * Checks whether the content from a given URL can be fetched.
     * <p>
     * Returns <code>false</code> if there is no access to the Internet.
     *
     * @param url of the resource to be fetched
     * @return true iff content from the given URL object can be fetched
     */
    private boolean isReachable(URL url) {
        try {
            url.openConnection().getContent();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Parses the login attempt <code>Response</code> and returns the corresponding <code>Login</code> object.
     *
     * @param response of the authentication HTTP request
     * @return a <code>Login</code> object parsed a login attempt from <code>response</code>
     * @throws IOException
     */
    private Login parseAuthResponse(Response response) throws IOException {
        String html = response.returnContent().asString();
        Document doc = Jsoup.parse(html);

        // Select span.error elements that are direct children of div.loginerrors
        Elements errors = doc.select("div.loginerrors > span.error");

        boolean successful;
        List<String> errorMessages;

        if (errors.size() == 0) {
            // Login was successful
            if (!html.contains("Tere tulemast")) {
                throw new RuntimeException("Encountered no errors during login, but didn't find 'Tere tulemast' message.");
            }
            successful = true;
            errorMessages = null;
        } else {
            // Login was not successful
            successful = false;
            errorMessages = new ArrayList<>();
            for (Element error : errors) {
                errorMessages.add(error.text());
            }
        }

        return new Login(successful, errorMessages);
    }


    /**
     * Checks if the given <code>Response</code> object represents a successful HTTP request response.
     *
     * @param response that represents the HTTP request whose successfulness is checked
     * @return true iff <code>response</code> represents a successful HTTP request
     * @throws IOException
     */
    private boolean isSuccessfulRequest(Response response) throws IOException {
        int responseCode = response.returnResponse().getStatusLine().getStatusCode();
        return responseCode >= 200 && responseCode < 400;
    }
}
