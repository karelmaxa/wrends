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
 *      Copyright 2006-2008 Sun Microsystems, Inc.
 *      Portions Copyright 2014 ForgeRock AS
 */
package org.opends.server.schema;



import static org.opends.messages.SchemaMessages.*;
import org.forgerock.i18n.slf4j.LocalizedLogger;
import static org.opends.server.schema.SchemaConstants.*;

import java.util.Collection;
import java.util.Collections;

import org.forgerock.i18n.LocalizableMessage;
import org.opends.server.api.EqualityMatchingRule;
import org.opends.server.core.DirectoryServer;
import org.forgerock.opendj.ldap.ByteSequence;
import org.forgerock.opendj.ldap.ByteString;
import org.opends.server.types.DirectoryException;
import org.opends.server.types.ResultCode;


/**
 * This class defines the bitStringMatch matching rule defined in X.520 and
 * referenced in RFC 2252.
 */
class BitStringEqualityMatchingRule
       extends EqualityMatchingRule
{

  private static final LocalizedLogger logger = LocalizedLogger.getLoggerForThisClass();

  /**
   * Creates a new instance of this bitStringMatch matching rule.
   */
  public BitStringEqualityMatchingRule()
  {
    super();
  }



  /**
   * {@inheritDoc}
   */
  @Override
  public Collection<String> getAllNames()
  {
    return Collections.singleton(getName());
  }



  /**
   * Retrieves the common name for this matching rule.
   *
   * @return  The common name for this matching rule, or <CODE>null</CODE> if
   * it does not have a name.
   */
  @Override
  public String getName()
  {
    return EMR_BIT_STRING_NAME;
  }



  /**
   * Retrieves the OID for this matching rule.
   *
   * @return  The OID for this matching rule.
   */
  @Override
  public String getOID()
  {
    return EMR_BIT_STRING_OID;
  }



  /**
   * Retrieves the description for this matching rule.
   *
   * @return  The description for this matching rule, or <CODE>null</CODE> if
   *          there is none.
   */
  @Override
  public String getDescription()
  {
    // There is no standard description for this matching rule.
    return null;
  }



  /**
   * Retrieves the OID of the syntax with which this matching rule is
   * associated.
   *
   * @return  The OID of the syntax with which this matching rule is associated.
   */
  @Override
  public String getSyntaxOID()
  {
    return SYNTAX_BIT_STRING_OID;
  }



  /**
   * Retrieves the normalized form of the provided value, which is best suited
   * for efficiently performing matching operations on that value.
   *
   * @param  value  The value to be normalized.
   *
   * @return  The normalized version of the provided value.
   *
   * @throws  DirectoryException  If the provided value is invalid according to
   *                              the associated attribute syntax.
   */
  @Override
  public ByteString normalizeValue(ByteSequence value)
         throws DirectoryException
  {
    String valueString = value.toString().toUpperCase();

    int length = valueString.length();
    if (length < 3)
    {
      return reportInvalidSyntax(valueString,
          WARN_ATTR_SYNTAX_BIT_STRING_TOO_SHORT.get(value));
    }

    if ((valueString.charAt(0) != '\'') ||
        (valueString.charAt(length-2) != '\'') ||
        (valueString.charAt(length-1) != 'B'))
    {
      return reportInvalidSyntax(valueString,
          WARN_ATTR_SYNTAX_BIT_STRING_NOT_QUOTED.get(value));
    }

    for (int i=1; i < (length-2); i++)
    {
      switch (valueString.charAt(i))
      {
        case '0':
        case '1':
          // These characters are fine.
          break;
        default:

        return reportInvalidSyntax(valueString,
            WARN_ATTR_SYNTAX_BIT_STRING_INVALID_BIT.get(value, valueString.charAt(i)));
      }
    }

    return ByteString.valueOf(valueString);
  }

  private ByteString reportInvalidSyntax(String valueString, LocalizableMessage message)
      throws DirectoryException
  {
    switch (DirectoryServer.getSyntaxEnforcementPolicy())
    {
      case REJECT:
        throw new DirectoryException(ResultCode.INVALID_ATTRIBUTE_SYNTAX, message);
      case WARN:
        logger.error(message);
        return ByteString.valueOf(valueString);
      default:
        return ByteString.valueOf(valueString);
    }
  }
}

