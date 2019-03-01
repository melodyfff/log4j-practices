package com.xinchen.log.appender;

import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConfigurationFactoryData;
import org.apache.logging.log4j.core.appender.ManagerFactory;
import org.apache.logging.log4j.core.appender.rolling.DirectWriteRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.FileExtension;
import org.apache.logging.log4j.core.appender.rolling.PatternProcessor;
import org.apache.logging.log4j.core.appender.rolling.RollingFileManager;
import org.apache.logging.log4j.core.appender.rolling.RolloverDescription;
import org.apache.logging.log4j.core.appender.rolling.RolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.TriggeringPolicy;
import org.apache.logging.log4j.core.appender.rolling.action.AbstractAction;
import org.apache.logging.log4j.core.appender.rolling.action.Action;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.util.Constants;
import org.apache.logging.log4j.core.util.FileUtils;
import org.apache.logging.log4j.core.util.Log4jThreadFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author xinchen
 * @version 1.0
 * @date 27/02/2019 16:24
 */
public class CustomerRollingFileManager extends RollingFileManager {

    private static final Map<String, CustomerRollingFileManager> MAP = new HashMap<>();
    private static final Lock LOCK = new ReentrantLock();

    private static CustomerRollingFileManager.RollingFileManagerFactory factory = new CustomerRollingFileManager.RollingFileManagerFactory();
    private static final int MAX_TRIES = 3;
    private static final int MIN_DURATION = 100;
    private long initialTime;
    private volatile PatternProcessor patternProcessor;
    private final Semaphore semaphore;
    private final Log4jThreadFactory threadFactory;
    private volatile TriggeringPolicy triggeringPolicy;
    private volatile RolloverStrategy rolloverStrategy;
    private volatile boolean renameEmptyFiles;
    private volatile boolean initialized;
    private volatile String fileName;
    private final FileExtension fileExtension;
    private final ExecutorService asyncExecutor;
    private static final AtomicReferenceFieldUpdater<CustomerRollingFileManager, TriggeringPolicy> triggeringPolicyUpdater = AtomicReferenceFieldUpdater.newUpdater(CustomerRollingFileManager.class, TriggeringPolicy.class, "triggeringPolicy");
    private static final AtomicReferenceFieldUpdater<CustomerRollingFileManager, RolloverStrategy> rolloverStrategyUpdater = AtomicReferenceFieldUpdater.newUpdater(CustomerRollingFileManager.class, RolloverStrategy.class, "rolloverStrategy");
    private static final AtomicReferenceFieldUpdater<CustomerRollingFileManager, PatternProcessor> patternProcessorUpdater = AtomicReferenceFieldUpdater.newUpdater(CustomerRollingFileManager.class, PatternProcessor.class, "patternProcessor");

    /**
     *
     * @param loggerContext
     * @param fileName
     * @param pattern
     * @param os
     * @param append
     * @param createOnDemand
     * @param size
     * @param time
     * @param triggeringPolicy
     * @param rolloverStrategy
     * @param advertiseURI
     * @param layout
     * @param filePermissions
     * @param fileOwner
     * @param fileGroup
     * @param writeHeader
     * @param buffer
     */
    public CustomerRollingFileManager(LoggerContext loggerContext, String fileName, String pattern, OutputStream os, boolean append, boolean createOnDemand, long size, long time, TriggeringPolicy triggeringPolicy, RolloverStrategy rolloverStrategy, String advertiseURI, Layout<? extends Serializable> layout, String filePermissions, String fileOwner, String fileGroup, boolean writeHeader, ByteBuffer buffer) {
        super(loggerContext, fileName, pattern, os, append, createOnDemand, size, time, triggeringPolicy, rolloverStrategy, advertiseURI, layout, filePermissions, fileOwner, fileGroup, writeHeader, buffer);
        this.semaphore = new Semaphore(1);
        this.threadFactory = Log4jThreadFactory.createThreadFactory("CustomerRollingFileManager");
        this.fileExtension = FileExtension.lookupForFile(pattern);
        this.asyncExecutor = new ThreadPoolExecutor(0, 2147483647, 0L, TimeUnit.MILLISECONDS, new CustomerRollingFileManager.EmptyQueue(), this.threadFactory);;
    }

    /**
     *
     * @param fileName
     * @param pattern
     * @param append
     * @param bufferedIO
     * @param policy
     * @param strategy
     * @param advertiseURI
     * @param layout
     * @param bufferSize
     * @param immediateFlush
     * @param createOnDemand
     * @param filePermissions
     * @param fileOwner
     * @param fileGroup
     * @param configuration
     * @return
     */
    public static CustomerRollingFileManager getMyFileManager(String originalFileName,String fileName, String pattern, boolean append, boolean bufferedIO, TriggeringPolicy policy, RolloverStrategy strategy, String advertiseURI, Layout<? extends Serializable> layout, int bufferSize, boolean immediateFlush, boolean createOnDemand, String filePermissions, String fileOwner, String fileGroup, Configuration configuration) {
        if (strategy instanceof DirectWriteRolloverStrategy && fileName != null) {
            LOGGER.error("The fileName attribute must not be specified with the DirectWriteRolloverStrategy");
            return null;
        } else {
            String name = fileName == null ? pattern : fileName;
            return getManage(originalFileName, new CustomerRollingFileManager.FactoryData(fileName, pattern, append, bufferedIO, policy, strategy, advertiseURI, layout, bufferSize, immediateFlush, createOnDemand, filePermissions, fileOwner, fileGroup, configuration),factory);
//            return narrow(CustomerRollingFileManager.class, getManager(name, , factory));
        }
    }

    /**
     *
     * @param name
     * @param data
     * @param factory
     * @return
     */
    public static CustomerRollingFileManager getManage(String name,CustomerRollingFileManager.FactoryData data,CustomerRollingFileManager.RollingFileManagerFactory factory){
        LOCK.lock();
        CustomerRollingFileManager customerRollingFileManager;
        try{
            CustomerRollingFileManager manager = MAP.get(name);
            if (null == manager){
                manager = factory.createManager(name, data);
                if (manager == null){
                    throw new IllegalStateException("ManagerFactory [" + factory + "] unable to create manager for [" + name + "] with data [" + data + "]");
                }
                MAP.put(name, manager);
            } else {
                manager.updateData(data);
            }
            ++manager.count;
            customerRollingFileManager = manager;
        } finally {
            LOCK.unlock();
        }
        return customerRollingFileManager;
    }


    /**
     *
     * @param data
     */
    @Override
    public void updateData(Object data) {
        CustomerRollingFileManager.FactoryData factoryData = (CustomerRollingFileManager.FactoryData)data;
        this.setRolloverStrategy(factoryData.getRolloverStrategy());
        this.setTriggeringPolicy(factoryData.getTriggeringPolicy());
        this.setPatternProcessor(new PatternProcessor(factoryData.getPattern(), this.getPatternProcessor()));
    }

    /**
     *
     * @return
     */
    @Override
    public String getFileName() {
        return fileName;
    }

    /**
     *
     * @param fileName
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    private boolean rollover(RolloverStrategy strategy) {
        boolean releaseRequired = false;

        try {
            this.semaphore.acquire();
            releaseRequired = true;
        } catch (InterruptedException var11) {
            this.logError("Thread interrupted while attempting to check rollover", var11);
            return false;
        }

        boolean success = true;

        boolean var5;
        try {
            RolloverDescription descriptor = strategy.rollover(this);
            if (descriptor != null) {
                this.writeFooter();
                this.closeOutputStream();
                if (descriptor.getSynchronous() != null) {
                    LOGGER.debug("RollingFileManager executing synchronous {}", descriptor.getSynchronous());

                    try {
                        success = descriptor.getSynchronous().execute();
                    } catch (Exception var10) {
                        success = false;
                        this.logError("Caught error in synchronous task", var10);
                    }
                }

                if (success && descriptor.getAsynchronous() != null) {
                    LOGGER.debug("RollingFileManager executing async {}", descriptor.getAsynchronous());
                    this.asyncExecutor.execute(new CustomerRollingFileManager.AsyncAction(descriptor.getAsynchronous(), this));
                    releaseRequired = false;
                }

                var5 = true;
                return var5;
            }

            var5 = false;
        } finally {
            if (releaseRequired) {
                this.semaphore.release();
            }

        }

        return var5;
    }

    private static class EmptyQueue extends ArrayBlockingQueue<Runnable> {
        private static final long serialVersionUID = 1L;

        EmptyQueue() {
            super(1);
        }

        @Override
        public int remainingCapacity() {
            return 0;
        }

        @Override
        public boolean add(Runnable runnable) {
            throw new IllegalStateException("Queue is full");
        }

        @Override
        public void put(Runnable runnable) throws InterruptedException {
            throw new InterruptedException("Unable to insert into queue");
        }

        @Override
        public boolean offer(Runnable runnable, long timeout, TimeUnit timeUnit) throws InterruptedException {
            Thread.sleep(timeUnit.toMillis(timeout));
            return false;
        }

        @Override
        public boolean addAll(Collection<? extends Runnable> collection) {
            if (collection.size() > 0) {
                throw new IllegalArgumentException("Too many items in collection");
            } else {
                return false;
            }
        }
    }

    private static class RollingFileManagerFactory implements ManagerFactory<CustomerRollingFileManager, FactoryData> {
        private RollingFileManagerFactory() {
        }

        @Override
        public CustomerRollingFileManager createManager(String name, CustomerRollingFileManager.FactoryData data) {
            long size = 0L;
            boolean writeHeader = !data.append;
            File file = null;
            if (data.fileName != null) {
                file = new File(data.fileName);
                writeHeader = !data.append || !file.exists();

                try {
                    FileUtils.makeParentDirs(file);
                    boolean created = !data.createOnDemand && file.createNewFile();
                    RollingFileManager.LOGGER.trace("New file '{}' created = {}", name, created);
                } catch (IOException var13) {
                    RollingFileManager.LOGGER.error("Unable to create file " + name, var13);
                    return null;
                }

                size = data.append ? file.length() : 0L;
            }

            try {
                int actualSize = data.bufferedIO ? data.bufferSize : Constants.ENCODER_BYTE_BUFFER_SIZE;
                ByteBuffer buffer = ByteBuffer.wrap(new byte[actualSize]);
                OutputStream os = !data.createOnDemand && data.fileName != null ? new FileOutputStream(data.fileName, data.append) : null;
                long time = !data.createOnDemand && file != null ? file.lastModified() : System.currentTimeMillis();
                CustomerRollingFileManager rm = new CustomerRollingFileManager(data.getLoggerContext(), data.fileName, data.pattern, os, data.append, data.createOnDemand, size, time, data.policy, data.strategy, data.advertiseURI, data.layout, data.filePermissions, data.fileOwner, data.fileGroup, writeHeader, buffer);
                if (os != null && rm.isAttributeViewEnabled()) {
                    rm.defineAttributeView(file.toPath());
                }

                return rm;
            } catch (IOException var14) {
                RollingFileManager.LOGGER.error("RollingFileManager (" + name + ") " + var14, var14);
                return null;
            }
        }
    }

    private static class FactoryData extends ConfigurationFactoryData {
        private final String fileName;
        private final String pattern;
        private final boolean append;
        private final boolean bufferedIO;
        private final int bufferSize;
        private final boolean immediateFlush;
        private final boolean createOnDemand;
        private final TriggeringPolicy policy;
        private final RolloverStrategy strategy;
        private final String advertiseURI;
        private final Layout<? extends Serializable> layout;
        private final String filePermissions;
        private final String fileOwner;
        private final String fileGroup;

        /**
         *
         * @param fileName
         * @param pattern
         * @param append
         * @param bufferedIO
         * @param policy
         * @param strategy
         * @param advertiseURI
         * @param layout
         * @param bufferSize
         * @param immediateFlush
         * @param createOnDemand
         * @param filePermissions
         * @param fileOwner
         * @param fileGroup
         * @param configuration
         */
        public FactoryData(String fileName, String pattern, boolean append, boolean bufferedIO, TriggeringPolicy policy, RolloverStrategy strategy, String advertiseURI, Layout<? extends Serializable> layout, int bufferSize, boolean immediateFlush, boolean createOnDemand, String filePermissions, String fileOwner, String fileGroup, Configuration configuration) {
            super(configuration);
            this.fileName = fileName;
            this.pattern = pattern;
            this.append = append;
            this.bufferedIO = bufferedIO;
            this.bufferSize = bufferSize;
            this.policy = policy;
            this.strategy = strategy;
            this.advertiseURI = advertiseURI;
            this.layout = layout;
            this.immediateFlush = immediateFlush;
            this.createOnDemand = createOnDemand;
            this.filePermissions = filePermissions;
            this.fileOwner = fileOwner;
            this.fileGroup = fileGroup;
        }

        public TriggeringPolicy getTriggeringPolicy() {
            return this.policy;
        }

        /**
         *
         * @return
         */
        public RolloverStrategy getRolloverStrategy() {
            return this.strategy;
        }

        /**
         *
         * @return String
         */
        public String getPattern() {
            return this.pattern;
        }

        /**
         *
         * @return String
         */
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(super.toString());
            builder.append("[pattern=");
            builder.append(this.pattern);
            builder.append(", append=");
            builder.append(this.append);
            builder.append(", bufferedIO=");
            builder.append(this.bufferedIO);
            builder.append(", bufferSize=");
            builder.append(this.bufferSize);
            builder.append(", policy=");
            builder.append(this.policy);
            builder.append(", strategy=");
            builder.append(this.strategy);
            builder.append(", advertiseURI=");
            builder.append(this.advertiseURI);
            builder.append(", layout=");
            builder.append(this.layout);
            builder.append(", filePermissions=");
            builder.append(this.filePermissions);
            builder.append(", fileOwner=");
            builder.append(this.fileOwner);
            builder.append("]");
            return builder.toString();
        }
    }

    private static class AsyncAction extends AbstractAction {
        private final Action action;
        private final CustomerRollingFileManager manager;

        /**
         *
         * @param act Action
         * @param manager CustomerRollingFileManager
         */
        public AsyncAction(Action act, CustomerRollingFileManager manager) {
            this.action = act;
            this.manager = manager;
        }

        /**
         *
         * @return boolean
         * @throws IOException IOException
         */
        @Override
        public boolean execute() throws IOException {
            boolean var1;
            try {
                var1 = this.action.execute();
            } finally {
                this.manager.semaphore.release();
            }

            return var1;
        }

        /**
         *
         */
        @Override
        public void close() {
            this.action.close();
        }
        /**
         *
         * @return boolean
         */
        @Override
        public boolean isComplete() {
            return this.action.isComplete();
        }

        /**
         *
         * @return String
         */
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(super.toString());
            builder.append("[action=");
            builder.append(this.action);
            builder.append(", manager=");
            builder.append(this.manager);
            builder.append(", isComplete()=");
            builder.append(this.isComplete());
            builder.append(", isInterrupted()=");
            builder.append(this.isInterrupted());
            builder.append("]");
            return builder.toString();
        }
    }
}

