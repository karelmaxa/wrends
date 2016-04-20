/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyright [year] [name of copyright owner]".
 *
 * Copyright 2008-2010 Sun Microsystems, Inc.
 * Portions Copyright 2014-2016 ForgeRock AS.
 */
package org.opends.guitools.controlpanel.ui.renderer;

import static org.opends.messages.AdminToolMessages.*;

import java.awt.Component;

import javax.swing.JComboBox;
import javax.swing.JList;

import org.forgerock.opendj.ldap.schema.Syntax;
import org.forgerock.opendj.ldap.schema.MatchingRule;
import org.forgerock.opendj.ldap.schema.AttributeUsage;
import org.opends.server.types.CommonSchemaElements;
import org.forgerock.opendj.ldap.schema.ObjectClassType;

/** The cell renderer to be used to render schema elements in a combo box. */
public class SchemaElementComboBoxCellRenderer extends CustomListCellRenderer
{
  /**
   * Constructor of the cell renderer.
   * @param combo the combo box containing the elements to be rendered.
   */
  public SchemaElementComboBoxCellRenderer(JComboBox combo)
  {
    super(combo);
  }

  /**
   * Constructor of the cell renderer.
   * @param list the list containing the elements to be rendered.
   */
  public SchemaElementComboBoxCellRenderer(JList list)
  {
    super(list);
  }

  @Override
  public Component getListCellRendererComponent(JList list, Object value,
      int index, boolean isSelected, boolean cellHasFocus)
  {
    if (value instanceof Syntax)
    {
      String syntaxName = ((Syntax)value).getName();
      if (syntaxName == null)
      {
        value = ((Syntax)value).getOID();
      }
      else
      {
        value = syntaxName;
      }
    }
    else if (value instanceof CommonSchemaElements)
    {
      value = ((CommonSchemaElements)value).getNameOrOID();
    }
    else if (value instanceof MatchingRule)
    {
      value = ((MatchingRule)value).getNameOrOID();
    }
    else if (value instanceof AttributeUsage)
    {
      boolean isOperational = ((AttributeUsage)value).isOperational();
      if (isOperational)
      {
        value = INFO_CTRL_PANEL_ATTRIBUTE_USAGE_OPERATIONAL.get(
            value.toString());
      }
    }
    else if (value instanceof ObjectClassType)
    {
      switch ((ObjectClassType)value)
      {
      case AUXILIARY:
        value = INFO_CTRL_PANEL_OBJECTCLASS_AUXILIARY_LABEL.get().toString();
        break;
      case STRUCTURAL:
        value = INFO_CTRL_PANEL_OBJECTCLASS_STRUCTURAL_LABEL.get().toString();
        break;
      case ABSTRACT:
        value = INFO_CTRL_PANEL_OBJECTCLASS_ABSTRACT_LABEL.get().toString();
        break;
      }
    }
    return super.getListCellRendererComponent(
        list, value, index, isSelected, cellHasFocus);
  }
}
