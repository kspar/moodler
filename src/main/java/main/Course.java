package main;

public class Course {

    private String id;
    private String name;

    public Course(String id, String name) {
        if (id == null) {
            throw new IllegalArgumentException("Id can't be null!");
        } else if (id.isEmpty()) {
            throw new IllegalArgumentException("Id can't be empty!");
        }
        if (name == null) {
            throw new IllegalArgumentException("Name can't be null!");
        } else if (name.isEmpty()) {
            throw new IllegalArgumentException("Name can't be empty!");
        }

        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Course{" +
                "name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Course course = (Course) o;

        if (id != null ? !id.equals(course.id) : course.id != null) return false;
        return !(name != null ? !name.equals(course.name) : course.name != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
