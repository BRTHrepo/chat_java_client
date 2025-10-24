package com.chatapp.core.model;

public class DeleteFriendResponse {


    /*
      ['error' => 'Friend not found']);
            return;
        }

        $responseData = [
            'message' => 'Friend removed successfully'
        ];

    }
     */
    private String message;
    private String error;

    public DeleteFriendResponse() {
    }
    public String getError() {
        return error;
    }
    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "DeletefriendResponse{" +
                "message='" + message + '\'' +
                ", error='" + error + '\'' +
                '}';
    }
}
