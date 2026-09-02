import org.testcontainers.DockerClientFactory;

public class TestDocker {
    public static void main(String[] args) {
        try {
            System.out.println("Testing Docker...");
            DockerClientFactory.instance().client();
            System.out.println("Docker client loaded!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
