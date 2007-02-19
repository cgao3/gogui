//----------------------------------------------------------------------------
// $Id$
//----------------------------------------------------------------------------

package net.sf.gogui.gui;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;
import net.sf.gogui.game.MarkType;
import net.sf.gogui.go.GoColor;
import net.sf.gogui.go.GoPoint;
import net.sf.gogui.util.ErrorMessage;

/** Context menu for fields on board. */
public class ContextMenu
    extends JPopupMenu
{
    /** Callback for events generated by ContextMenu. */
    public interface Listener
    {
        void actionClearAnalyzeCommand();

        void actionEditLabel(GoPoint point);

        void actionMark(GoPoint point, MarkType type, boolean mark);

        void actionSetAnalyzeCommand(AnalyzeCommand command);
    }

    public ContextMenu(GoPoint point, boolean noProgram,
                       ArrayList supportedCommands, File analyzeCommands,
                       String programAnalyzeCommands, boolean mark,
                       boolean markCircle, boolean markSquare,
                       boolean markTriangle, Listener listener)
    {
        m_point = point;
        m_listener = listener;
        ArrayList commands = new ArrayList();
        ArrayList labels = new ArrayList();
        if (! noProgram)
        {
            try
            {
                AnalyzeCommand.read(commands, labels, supportedCommands,
                                    analyzeCommands, programAnalyzeCommands);
            }
            catch (ErrorMessage e)
            {
            }
        }
        m_actionListener = new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    Listener listener = ContextMenu.this.m_listener;
                    if (listener == null)
                        return;
                    String actionCommand = event.getActionCommand();
                    if (actionCommand.equals("analyze-clear"))
                        listener.actionClearAnalyzeCommand();
                    else if (actionCommand.equals("cancel"))
                    {
                        ContextMenu.this.setVisible(false);
                    }
                    else if (actionCommand.equals("mark"))
                    {                        
                        boolean mark
                            = ContextMenu.this.m_mark.isSelected();
                        listener.actionMark(m_point, MarkType.MARK, mark);
                    }
                    else if (actionCommand.equals("mark-circle"))
                    {                        
                        boolean mark
                            = ContextMenu.this.m_markCircle.isSelected();
                        listener.actionMark(m_point, MarkType.CIRCLE, mark);
                    }
                    else if (actionCommand.equals("mark-square"))
                    {                        
                        boolean mark
                            = ContextMenu.this.m_markSquare.isSelected();
                        listener.actionMark(m_point, MarkType.SQUARE, mark);
                    }
                    else if (actionCommand.equals("mark-triangle"))
                    {                        
                        boolean mark
                            = ContextMenu.this.m_markTriangle.isSelected();
                        listener.actionMark(m_point, MarkType.TRIANGLE, mark);
                    }
                    else if (actionCommand.equals("edit-label"))
                    {                        
                        listener.actionEditLabel(m_point);
                    }
                    else
                    {
                        int index = Integer.parseInt(actionCommand);
                        AnalyzeCommand command = getCommand(index);
                        command.setPointArg(m_point);
                        listener.actionSetAnalyzeCommand(command);
                    }
                }
            };
        JLabel label = new JLabel("Point " + point);
        label.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD));
        label.setBorder(GuiUtil.createSmallEmptyBorder());
        add(label);
        addSeparator();
        m_mark = createCheckBox("Mark", "mark");
        m_mark.setSelected(mark);
        add(m_mark);
        m_markCircle = createCheckBox("Mark Circle", "mark-circle");
        m_markCircle.setSelected(markCircle);
        add(m_markCircle);
        m_markSquare = createCheckBox("Mark Square", "mark-square");
        m_markSquare.setSelected(markSquare);
        add(m_markSquare);
        m_markTriangle = createCheckBox("Mark Triangle", "mark-triangle");
        m_markTriangle.setSelected(markTriangle);
        add(m_markTriangle);
        add(createItem("Edit Label", "edit-label"));
        addSeparator();
        if (! noProgram && commands.size() > 0)
        {
            m_analyzeMenu = new JMenu("Analyze at " + point);
            // For com.jgoodies.looks
            m_analyzeMenu.putClientProperty("jgoodies.noIcons", Boolean.TRUE);
            add(m_analyzeMenu);            
            for (int i = 0; i < commands.size(); ++i)
            {
                String line = (String)commands.get(i);
                AnalyzeCommand command = new AnalyzeCommand(line);
                if (command.needsOnlyPointArg())
                    addCommand(command);
                else if (command.needsOnlyPointAndColorArg())
                    addColorCommand(command);
            }
            JMenuItem analyzeClear =
                createItem("Analyze Clear", "analyze-clear");
            add(analyzeClear);
            if (m_analyzeMenu.getMenuComponentCount() == 0)
            {
                m_analyzeMenu.setEnabled(false);
                analyzeClear.setEnabled(false);
            }
            addSeparator();
        }
        else
            m_analyzeMenu = null;
        JMenuItem item = new JMenuItem("Cancel");
        item.addActionListener(m_actionListener);
        item.setActionCommand("cancel");
        add(item);
    }

    public GoPoint getPointArg()
    {
        return m_point;
    }

    public boolean isEmpty()
    {
        return (m_commands.size() == 0);
    }

    /** Serial version to suppress compiler warning.
        Contains a marker comment for serialver.sourceforge.net
    */
    private static final long serialVersionUID = 0L; // SUID

    private final ActionListener m_actionListener;

    private final GoPoint m_point;

    private final JCheckBoxMenuItem m_mark;

    private final JCheckBoxMenuItem m_markCircle;

    private final JCheckBoxMenuItem m_markSquare;

    private final JCheckBoxMenuItem m_markTriangle;

    private final JMenu m_analyzeMenu;

    private final Listener m_listener;

    private final ArrayList m_commands = new ArrayList();

    private void addColorCommand(AnalyzeCommand command)
    {
        String label = command.getLabel();
        JMenu menu = new JMenu(label);
        // For com.jgoodies.looks
        menu.putClientProperty("jgoodies.noIcons", Boolean.TRUE);
        command.setColorArg(GoColor.BLACK);
        JMenuItem item = createItem(command, "Black");
        menu.add(item);
        command = command.cloneCommand();
        command.setColorArg(GoColor.WHITE);
        item = createItem(command, "White");
        menu.add(item);
        m_analyzeMenu.add(menu);
    }

    private void addCommand(AnalyzeCommand command)
    {        
        JMenuItem item = createItem(command, command.getLabel());
        m_analyzeMenu.add(item);
    }

    private JMenuItem createItem(AnalyzeCommand command, String label)
    {
        assert(! m_commands.contains(command));
        m_commands.add(command);
        return createItem(label, Integer.toString(m_commands.size() - 1));
    }

    private JCheckBoxMenuItem createCheckBox(String label,
                                             String actionCommand)
    {
        JCheckBoxMenuItem item = new JCheckBoxMenuItem(label);
        item.addActionListener(m_actionListener);
        item.setActionCommand(actionCommand);
        return item;
    }

    private JMenuItem createItem(String label, String actionCommand)
    {
        JMenuItem item = new JMenuItem(label);
        item.addActionListener(m_actionListener);
        item.setActionCommand(actionCommand);
        return item;
    }

    private AnalyzeCommand getCommand(int index)
    {
        return (AnalyzeCommand)m_commands.get(index);
    }
}

