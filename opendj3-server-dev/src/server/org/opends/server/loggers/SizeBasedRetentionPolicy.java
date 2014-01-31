/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at legal-notices/CDDLv1_0.txt
 * or http://forgerock.org/license/CDDLv1.0.html.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at legal-notices/CDDLv1_0.txt.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information:
 *      Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 *
 *
 *      Copyright 2006-2009 Sun Microsystems, Inc.
 *      Portions Copyright 2014 ForgeRock AS
 */
package org.opends.server.loggers;
import static org.opends.messages.LoggerMessages.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.forgerock.i18n.LocalizableMessage;
import org.forgerock.i18n.slf4j.LocalizedLogger;
import org.opends.server.admin.server.ConfigurationChangeListener;
import org.opends.server.admin.std.server.SizeLimitLogRetentionPolicyCfg;
import org.opends.server.core.DirectoryServer;
import org.opends.server.types.ConfigChangeResult;
import org.opends.server.types.DirectoryException;
import org.opends.server.types.ResultCode;

/**
 * This class implements a retention policy based on the amount of
 * space taken by the log files.
 */
public class SizeBasedRetentionPolicy implements
    RetentionPolicy<SizeLimitLogRetentionPolicyCfg>,
    ConfigurationChangeListener<SizeLimitLogRetentionPolicyCfg>
{

  private static final LocalizedLogger logger = LocalizedLogger.getLoggerForThisClass();
  private static final File[] EMPTY_FILE_LIST = new File[0];

  private long size = 0;
  private FileComparator comparator;
  private SizeLimitLogRetentionPolicyCfg config;

  /**
   * {@inheritDoc}
   */
  public void initializeLogRetentionPolicy(
      SizeLimitLogRetentionPolicyCfg config)
  {
    this.size = config.getDiskSpaceUsed();
    this.comparator = new FileComparator();
    this.config = config;

    config.addSizeLimitChangeListener(this);
  }

  /**
   * {@inheritDoc}
   */
  public boolean isConfigurationChangeAcceptable(
      SizeLimitLogRetentionPolicyCfg config,
      List<LocalizableMessage> unacceptableReasons)
  {
    // Changes should always be OK
    return true;
  }

  /**
   * {@inheritDoc}
   */
  public ConfigChangeResult applyConfigurationChange(
      SizeLimitLogRetentionPolicyCfg config)
  {
    // Default result code.
    ResultCode resultCode = ResultCode.SUCCESS;
    boolean adminActionRequired = false;
    ArrayList<LocalizableMessage> messages = new ArrayList<LocalizableMessage>();

    this.size = config.getDiskSpaceUsed();
    this.config = config;

    return new ConfigChangeResult(resultCode, adminActionRequired, messages);
  }

  /**
   * {@inheritDoc}
   */
  public File[] deleteFiles(FileNamingPolicy fileNamingPolicy)
      throws DirectoryException
  {
    File[] files = fileNamingPolicy.listFiles();
    if(files == null)
    {
      throw new DirectoryException(DirectoryServer.getServerErrorResultCode(),
          ERR_LOGGER_ERROR_LISTING_FILES.get(fileNamingPolicy.getInitialName()));
    }

    long totalLength = 0;
    for (File file : files)
    {
      totalLength += file.length();
    }

    logger.trace("Total size of files: %d, Max: %d", totalLength, size);

    if (totalLength <= size)
    {
      return EMPTY_FILE_LIST;
    }

    long freeSpaceNeeded = totalLength - size;

    // Sort files based on last modified time.
    Arrays.sort(files, comparator);

    long freedSpace = 0;
    int j;
    for (j = files.length - 1; j >= 0; j--)
    {
      freedSpace += files[j].length();
      if (freedSpace >= freeSpaceNeeded)
      {
        break;
      }
    }

    File[] filesToDelete = new File[files.length - j];
    System.arraycopy(files, j, filesToDelete, 0, filesToDelete.length);
    return filesToDelete;
  }

  /**
   * {@inheritDoc}
   */
  public String toString()
  {
    return "Size Based Retention Policy " + config.dn().toString();
  }
}

