package com.xinchen.log;

import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.helpers.CountingQuietWriter;
import org.apache.log4j.helpers.LogLog;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 按照日期和大小生成日志文件
 * @author xinchen
 * @version 1.0
 * @date 30/01/2019 16:49
 */
public class TimeAndRollingFileAppender extends RollingFileAppender {
    private long nextRollover = 0L;

    private String datePattern = ".yyyyMMdd";
    private String level = ".info";
    private String suffix = ".log";
    private String original = "";


    @Override
    public void setFile(String file) {
        String val = file.trim();
        this.original = val;
        this.fileName = this.nameBuilder(1);
    }

    @Override
    public void rollOver() {
        if (this.qw != null) {
            long size = ((CountingQuietWriter)this.qw).getCount();
            LogLog.debug("rolling over count=" + size);
            this.nextRollover = size + this.maxFileSize;
        }

        LogLog.debug("maxBackupIndex=" + this.maxBackupIndex);
        boolean renameSucceeded = true;
        if (this.maxBackupIndex > 0) {
            File file = new File(this.nameBuilder(this.maxBackupIndex));
            if (file.exists()) {
                renameSucceeded = file.delete();
            }

            File target;
            for(int i = this.maxBackupIndex - 1; i >= 1 && renameSucceeded; --i) {
                file = new File(this.nameBuilder(i));
                if (file.exists()) {
                    target = new File(this.nameBuilder(i + 1));
                    LogLog.debug("Renaming file " + file + " to " + target);
                    renameSucceeded = file.renameTo(target);
                }
            }

            if (renameSucceeded) {
                target = new File(this.nameBuilder(1));
                this.closeFile();
                file = new File(this.fileName);
                LogLog.debug("Renaming file " + file + " to " + target);
                renameSucceeded = file.renameTo(target);
                if (!renameSucceeded) {
                    try {
                        this.setFile(this.fileName, true, this.bufferedIO, this.bufferSize);
                    } catch (IOException var6) {
                        if (var6 instanceof InterruptedIOException) {
                            Thread.currentThread().interrupt();
                        }

                        LogLog.error("setFile(" + this.fileName + ", true) call failed.", var6);
                    }
                }
            }
        }

        if (renameSucceeded) {
            try {
                this.setFile(this.fileName, false, this.bufferedIO, this.bufferSize);
                this.nextRollover = 0L;
            } catch (IOException var5) {
                if (var5 instanceof InterruptedIOException) {
                    Thread.currentThread().interrupt();
                }

                LogLog.error("setFile(" + this.fileName + ", false) call failed.", var5);
            }
        }
    }

    @Override
    public synchronized void setFile(String fileName, boolean append, boolean bufferedIO, int bufferSize) throws IOException {
        if (this.original.equals(this.fileName)){
            this.fileName = this.nameBuilder(1);
        }
        super.setFile(fileName, append, bufferedIO, bufferSize);
    }

    public String getDatePattern() {
        return datePattern;
    }

    public void setDatePattern(String datePattern) {
        this.datePattern = datePattern;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    /**
     * 构建文件名,按照时间和备份号
     * @param backupIndex 备份尾号
     * @return 文件名 + 日期格式 + 日志等级 + 编号(%03d) + 尾缀
     */
    public String nameBuilder(int backupIndex){
        // 前缀 + 日期格式 + 日志等级 + 编号 + 结尾
        return this.original +
                LocalDate.now().format(DateTimeFormatter.ofPattern(this.datePattern)) +
                this.level +
                String.format(".%03d", backupIndex) +
                this.suffix;
    }
}
