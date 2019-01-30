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

    protected String datePattern = "'.'yyyy-MM-dd";

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
            File file = new File(this.fileName + LocalDate.now().format(DateTimeFormatter.ofPattern(datePattern)) + this.maxBackupIndex);
            if (file.exists()) {
                renameSucceeded = file.delete();
            }

            File target;
            for(int i = this.maxBackupIndex - 1; i >= 1 && renameSucceeded; --i) {
                file = new File(this.fileName  + LocalDate.now().format(DateTimeFormatter.ofPattern(datePattern))+ "." + i);
                if (file.exists()) {
                    target = new File(this.fileName + LocalDate.now().format(DateTimeFormatter.ofPattern(datePattern)) + "."+ (i + 1));
                    LogLog.debug("Renaming file " + file + " to " + target);
                    renameSucceeded = file.renameTo(target);
                }
            }

            if (renameSucceeded) {
                target = new File(this.fileName + LocalDate.now().format(DateTimeFormatter.ofPattern(datePattern))+ "." + 1);
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

    public String getDatePattern() {
        return datePattern;
    }

    public void setDatePattern(String datePattern) {
        this.datePattern = datePattern;
    }
}
