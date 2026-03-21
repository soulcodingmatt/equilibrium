// package io.github.soulcodingmatt.equilibrium.annotations.dto;
//
// import io.github.soulcodingmatt.equilibrium.annotations.dto.DtoBuilderDefault;
// import io.github.soulcodingmatt.equilibrium.annotations.dto.GenerateDto;
//
/// **
// * Test class demonstrating the enhanced @DtoBuilderDefault functionality.
// * This class shows how the annotation can be used with different field types
// * and how the annotation processor handles type conversion and validation.
// */
// @GenerateDto(builder = true)
// public class DtoBuilderDefaultTest {
//
//    // Static final variables for enum constants (type-safe approach)
//    public static final String STATUS_ACTIVE = "Status.ACTIVE";
//    public static final String STATUS_INACTIVE = "Status.INACTIVE";
//    public static final String STATUS_PENDING = "Status.PENDING";
//
//    // NEW: Type-specific parameters (no quotes needed!)
//    @DtoBuilderDefault(intValue = 42)
//    private int count;
//
//    @DtoBuilderDefault(booleanValue = true)
//    private boolean active;
//
//    @DtoBuilderDefault(doubleValue = 3.14)
//    private double pi;
//
//    @DtoBuilderDefault(charValue = 'A')
//    private char grade;
//
//    @DtoBuilderDefault(longValue = 100L)
//    private long timestamp;
//
//    @DtoBuilderDefault(floatValue = 1.5f)
//    private float ratio;
//
//    @DtoBuilderDefault(byteValue = 127)
//    private byte flags;
//
//    @DtoBuilderDefault(shortValue = 32767)
//    private short port;
//
//    // Wrapper types using primitive parameters
//    @DtoBuilderDefault(intValue = 42)
//    private Integer countWrapper;
//
//    @DtoBuilderDefault(booleanValue = true)
//    private Boolean activeWrapper;
//
//    @DtoBuilderDefault(doubleValue = 3.14)
//    private Double piWrapper;
//
//    @DtoBuilderDefault(charValue = 'A')
//    private Character gradeWrapper;
//
//    // String fields with type-specific parameter
//    @DtoBuilderDefault(stringValue = "Hello World")
//    private String message;
//
//    @DtoBuilderDefault(stringValue = "\"Already quoted\"")
//    private String preQuoted;
//
//    // Enum fields with type-specific parameters
//    @DtoBuilderDefault(enumValue = "Status.ACTIVE")
//    private Status status;
//
//    @DtoBuilderDefault(enumValue = "Status.INACTIVE")
//    private Status statusQualified;
//
//    // Limited enum approach (uses first enum constant)
//    @DtoBuilderDefault(enumValue =
// "io.github.soulcodingmatt.equilibrium.annotations.dto.DtoBuilderDefaultTest$Status")
//    private Status limitedStatus;
//
//    // LEGACY: String-based approach (still supported for backward compatibility)
//    @DtoBuilderDefault(value = "42")
//    private int legacyCount;
//
//    @DtoBuilderDefault(value = "true")
//    private boolean legacyActive;
//
//    @DtoBuilderDefault(value = "Hello")
//    private String legacyMessage;
//
//    @DtoBuilderDefault(value = "ACTIVE")
//    private Status legacyStatus;
//
//    // Collections - use standard defaults
//    @DtoBuilderDefault
//    private java.util.List<String> tags;
//
//    @DtoBuilderDefault
//    private java.util.Set<Integer> numbers;
//
//    @DtoBuilderDefault
//    private java.util.Map<String, Object> metadata;
//
//    // Optional - use standard default
//    @DtoBuilderDefault
//    private java.util.Optional<String> description;
//
//    // Custom enum for testing
//    public enum Status {
//        ACTIVE, INACTIVE, PENDING
//    }
//
//    // Getters and setters
//    public int getCount() { return count; }
//    public void setCount(int count) { this.count = count; }
//
//    public boolean isActive() { return active; }
//    public void setActive(boolean active) { this.active = active; }
//
//    public double getPi() { return pi; }
//    public void setPi(double pi) { this.pi = pi; }
//
//    public char getGrade() { return grade; }
//    public void setGrade(char grade) { this.grade = grade; }
//
//    public long getTimestamp() { return timestamp; }
//    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
//
//    public float getRatio() { return ratio; }
//    public void setRatio(float ratio) { this.ratio = ratio; }
//
//    public byte getFlags() { return flags; }
//    public void setFlags(byte flags) { this.flags = flags; }
//
//    public short getPort() { return port; }
//    public void setPort(short port) { this.port = port; }
//
//    public Integer getCountWrapper() { return countWrapper; }
//    public void setCountWrapper(Integer countWrapper) { this.countWrapper = countWrapper; }
//
//    public Boolean getActiveWrapper() { return activeWrapper; }
//    public void setActiveWrapper(Boolean activeWrapper) { this.activeWrapper = activeWrapper; }
//
//    public Double getPiWrapper() { return piWrapper; }
//    public void setPiWrapper(Double piWrapper) { this.piWrapper = piWrapper; }
//
//    public Character getGradeWrapper() { return gradeWrapper; }
//    public void setGradeWrapper(Character gradeWrapper) { this.gradeWrapper = gradeWrapper; }
//
//    public String getMessage() { return message; }
//    public void setMessage(String message) { this.message = message; }
//
//    public String getPreQuoted() { return preQuoted; }
//    public void setPreQuoted(String preQuoted) { this.preQuoted = preQuoted; }
//
//    public Status getStatus() { return status; }
//    public void setStatus(Status status) { this.status = status; }
//
//    public Status getStatusQualified() { return statusQualified; }
//    public void setStatusQualified(Status statusQualified) { this.statusQualified =
// statusQualified; }
//
//    // Legacy getters and setters
//    public int getLegacyCount() { return legacyCount; }
//    public void setLegacyCount(int legacyCount) { this.legacyCount = legacyCount; }
//
//    public boolean isLegacyActive() { return legacyActive; }
//    public void setLegacyActive(boolean legacyActive) { this.legacyActive = legacyActive; }
//
//    public String getLegacyMessage() { return legacyMessage; }
//    public void setLegacyMessage(String legacyMessage) { this.legacyMessage = legacyMessage; }
//
//    public Status getLegacyStatus() { return legacyStatus; }
//    public void setLegacyStatus(Status legacyStatus) { this.legacyStatus = legacyStatus; }
//
//    public java.util.List<String> getTags() { return tags; }
//    public void setTags(java.util.List<String> tags) { this.tags = tags; }
//
//    public java.util.Set<Integer> getNumbers() { return numbers; }
//    public void setNumbers(java.util.Set<Integer> numbers) { this.numbers = numbers; }
//
//    public java.util.Map<String, Object> getMetadata() { return metadata; }
//    public void setMetadata(java.util.Map<String, Object> metadata) { this.metadata = metadata; }
//
//    public java.util.Optional<String> getDescription() { return description; }
//    public void setDescription(java.util.Optional<String> description) { this.description =
// description; }
// }
