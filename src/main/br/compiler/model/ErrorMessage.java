package br.compiler.model;

/**
 * Representa uma mensagem de erro de compilação
 * 
 * @author Jordan Lippert
 * @author André Melo
 */
public class ErrorMessage {
    private final String type;
    private final String message;
    private final int position;
    private final int line;
    private final ErrorSeverity severity;
    private String formattedMessage;

    private ErrorMessage(Builder builder) {
        this.type = builder.type;
        this.message = builder.message;
        this.position = builder.position;
        this.line = builder.line;
        this.severity = builder.severity;
        this.formattedMessage = builder.formattedMessage;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public int getPosition() {
        return position;
    }

    public int getLine() {
        return line;
    }

    public ErrorSeverity getSeverity() {
        return severity;
    }

    public String getFormattedMessage() {
        return formattedMessage != null ? formattedMessage : formatDefault();
    }

    public void setFormattedMessage(String formattedMessage) {
        this.formattedMessage = formattedMessage;
    }

    private String formatDefault() {
        return String.format("[%s] Linha %d: %s", type, line, message);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String type = "ERRO";
        private String message = "";
        private int position = -1;
        private int line = -1;
        private ErrorSeverity severity = ErrorSeverity.ERROR;
        private String formattedMessage;

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder position(int position) {
            this.position = position;
            return this;
        }

        public Builder line(int line) {
            this.line = line;
            return this;
        }

        public Builder severity(ErrorSeverity severity) {
            this.severity = severity;
            return this;
        }

        public Builder formattedMessage(String formattedMessage) {
            this.formattedMessage = formattedMessage;
            return this;
        }

        public ErrorMessage build() {
            return new ErrorMessage(this);
        }
    }

    @Override
    public String toString() {
        return getFormattedMessage();
    }
}
