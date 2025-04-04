package boerenkool.communication.dto;

public class BlockedUserDTO {
    private String username;
    private int userId;

    public BlockedUserDTO(int userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public int getUserId() {
        return userId;
    }
}
