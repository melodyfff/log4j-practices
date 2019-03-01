package com.xinchen.log.appender;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractOutputStreamAppender;
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.DirectFileRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.DirectWriteRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.RolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.TriggeringPolicy;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.apache.logging.log4j.core.net.Advertiser;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 自定义Appender
 *
 * 生成的初始文件名按照日期滚动
 *
 * @author xinchen
 * @version 1.0
 * @date 27/02/2019 09:53
 */
@Plugin(
        name = "CustomerAppender",
        category = "Core",
        elementType = "appender",
        printObject = true
)

public final class CustomerAppender extends AbstractOutputStreamAppender<CustomerRollingFileManager> {
    /***/
    public static final String PLUGIN_NAME = "CustomerAppender";
    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private String fileName;
    private final String filePattern;
    private Object advertisement;
    private final Advertiser advertiser;
    private String originalFileName;

    private static final String YYYYMMDDHHMM = "yyyyMMddHHmm";
    private static final String YYYYMMDDHH = "yyyyMMddHH";
    private static final String YYYYMMDD = "yyyyMMdd";
    /***/
    public CustomerAppender(String name, Layout<? extends Serializable> layout, Filter filter,
                            CustomerRollingFileManager manager, String fileName, String originalFileName,
                            String filePattern,
                            boolean ignoreExceptions, boolean immediateFlush, Advertiser advertiser) {
        super(name, layout, filter, ignoreExceptions, immediateFlush, manager);
        if (advertiser != null) {
            Map<String, String> configuration = new HashMap<>(layout.getContentFormat());
            configuration.put("contentType", layout.getContentType());
            configuration.put("name", name);
            this.advertisement = advertiser.advertise(configuration);
        }

        this.fileName = fileName;
        this.originalFileName = originalFileName;
        this.filePattern = filePattern;
        this.advertiser = advertiser;
    }

    /***/
    public static String checkFileName(String originalFileName){
        if (originalFileName.contains(YYYYMMDDHHMM)){
            return originalFileName.replace(YYYYMMDDHHMM,
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern(YYYYMMDDHHMM)));
        }else if (originalFileName.contains(YYYYMMDDHH)){
            return originalFileName.replace(YYYYMMDDHH,
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern(YYYYMMDDHH)));
        }else if (originalFileName.contains(YYYYMMDD)){
            return originalFileName.replace(YYYYMMDD,
                    LocalDate.now().format(DateTimeFormatter.ofPattern(YYYYMMDD)));
        }
        return null;
    }
    /***/
    @Override
    public void append(LogEvent event) {
        (this.getManager()).setFileName(checkFileName(this.originalFileName));
        this.getManager().checkRollover(event);
        super.append(event);
    }
    /***/
    public String getFileName() {
        return this.fileName;
    }
    /***/
    public String getFilePattern() {
        return this.filePattern;
    }
    /***/
    public Object getAdvertisement() {
        return advertisement;
    }
    /***/
    public Advertiser getAdvertiser() {
        return advertiser;
    }

    /***/
    public <T extends TriggeringPolicy> T getTriggeringPolicy() {
        return ((CustomerRollingFileManager)this.getManager()).getTriggeringPolicy();
    }
    /***/
    @PluginBuilderFactory
    public static <B extends CustomerAppender.Builder<B>> Builder newBuilder() {
        return (CustomerAppender.Builder) (new Builder()).asBuilder();
    }
    /***/
    public static class Builder<B extends CustomerAppender.Builder<B>> extends
            AbstractOutputStreamAppender.Builder<B>
            implements org.apache.logging.log4j.core.util.Builder<CustomerAppender> {
        @PluginBuilderAttribute
        private String fileName;
        @PluginBuilderAttribute
        @Required
        private String filePattern;
        @PluginBuilderAttribute
        private boolean append = true;
        @PluginBuilderAttribute
        private String datePattern;
        @PluginBuilderAttribute
        private boolean locking;
        @PluginElement("Policy")
        @Required
        private TriggeringPolicy policy;
        @PluginElement("Strategy")
        private RolloverStrategy strategy;
        @PluginBuilderAttribute
        private boolean advertise;
        @PluginBuilderAttribute
        private String advertiseUri;
        @PluginBuilderAttribute
        private boolean createOnDemand;
        @PluginBuilderAttribute
        private String filePermissions;
        @PluginBuilderAttribute
        private String fileOwner;
        @PluginBuilderAttribute
        private String fileGroup;
        /***/
        public Builder() {
        }
        /***/
        @Override
        public CustomerAppender build() {
            boolean isBufferedIo = this.isBufferedIo();
            int bufferSize = this.getBufferSize();
            if (this.getName() == null) {
                CustomerAppender.LOGGER.error("RollingFileAppender '{}': No name provided.", this.getName());
                return null;
            } else {
                if (!isBufferedIo && bufferSize > 0) {
                    CustomerAppender.LOGGER.warn("RollingFileAppender '{}': " +
                            "The bufferSize is set to {} but bufferedIO is not true", this.getName(), bufferSize);
                }

                if (this.filePattern == null) {
                    CustomerAppender.LOGGER.error("RollingFileAppender '{}': " +
                            "No file name pattern provided.", this.getName());
                    return null;
                } else if (this.policy == null) {
                    CustomerAppender.LOGGER.error("RollingFileAppender '{}': " +
                            "No TriggeringPolicy provided.", this.getName());
                    return null;
                } else {
                    if (this.strategy == null) {
                        if (this.fileName != null) {
                            this.strategy = DefaultRolloverStrategy.newBuilder()
                                    .withCompressionLevelStr(String.valueOf(-1))
                                    .withConfig(this.getConfiguration()).build();
                        } else {
                            this.strategy = DirectWriteRolloverStrategy.newBuilder()
                                    .withCompressionLevelStr(String.valueOf(-1))
                                    .withConfig(this.getConfiguration()).build();
                        }
                    } else if (this.fileName == null && !(this.strategy instanceof DirectFileRolloverStrategy)) {
                        CustomerAppender.LOGGER.error("RollingFileAppender '{}':" +
                                " When no file name is provided a DirectFilenameRolloverStrategy must be configured");
                        return null;
                    }


                    Layout<? extends Serializable> layout = this.getOrCreateLayout();
                    CustomerRollingFileManager manager = CustomerRollingFileManager.getMyFileManager(fileName,
                            checkFileName(fileName), this.filePattern, this.append, isBufferedIo, this.policy,
                            this.strategy, this.advertiseUri, layout, bufferSize, this.isImmediateFlush(),
                            this.createOnDemand, this.filePermissions, this.fileOwner,
                            this.fileGroup, this.getConfiguration());
                    if (manager == null) {
                        return null;
                    } else {
                        manager.initialize();

                        return new CustomerAppender(this.getName(), layout, this.getFilter(),
                                manager, checkFileName(fileName),this.fileName, this.filePattern,
                                this.isIgnoreExceptions(),
                                this.isImmediateFlush(), this.advertise ?
                                this.getConfiguration().getAdvertiser() : null);
                    }
                }
            }
        }
        /***/
        public String getAdvertiseUri() {
            return this.advertiseUri;
        }
        /***/
        public String getFileName() {
            return this.fileName;
        }
        /***/
        public boolean isAdvertise() {
            return this.advertise;
        }
        /***/
        public boolean isAppend() {
            return this.append;
        }
        /***/
        public boolean isCreateOnDemand() {
            return this.createOnDemand;
        }
        /***/
        public boolean isLocking() {
            return this.locking;
        }
        /***/
        public String getFilePermissions() {
            return this.filePermissions;
        }
        /***/
        public String getFileOwner() {
            return this.fileOwner;
        }
        /***/
        public String getFileGroup() {
            return this.fileGroup;
        }
        /***/
        public B withAdvertise(boolean advertise) {
            this.advertise = advertise;
            return this.asBuilder();
        }
        /***/
        public B withAdvertiseUri(String advertiseUri) {
            this.advertiseUri = advertiseUri;
            return this.asBuilder();
        }
        /***/
        public B withAppend(boolean append) {
            this.append = append;
            return this.asBuilder();
        }
        /***/
        public B withFileName(String fileName) {
            this.fileName = fileName;
            return this.asBuilder();
        }

        /***/
        public B withCreateOnDemand(boolean createOnDemand) {
            this.createOnDemand = createOnDemand;
            return this.asBuilder();
        }
        /***/
        public B withLocking(boolean locking) {
            this.locking = locking;
            return this.asBuilder();
        }
        /***/
        public String getFilePattern() {
            return this.filePattern;
        }
        /***/
        public TriggeringPolicy getPolicy() {
            return this.policy;
        }
        /***/
        public RolloverStrategy getStrategy() {
            return this.strategy;
        }
        /***/
        public B withFilePattern(String filePattern) {
            this.filePattern = filePattern;
            return this.asBuilder();
        }
        /***/
        public B withPolicy(TriggeringPolicy policy) {
            this.policy = policy;
            return this.asBuilder();
        }
        /***/
        public B withStrategy(RolloverStrategy strategy) {
            this.strategy = strategy;
            return this.asBuilder();
        }
        /***/
        public B withFilePermissions(String filePermissions) {
            this.filePermissions = filePermissions;
            return this.asBuilder();
        }
        /***/
        public B withFileOwner(String fileOwner) {
            this.fileOwner = fileOwner;
            return this.asBuilder();
        }
        /***/
        public B withFileGroup(String fileGroup) {
            this.fileGroup = fileGroup;
            return this.asBuilder();
        }
    }

}
