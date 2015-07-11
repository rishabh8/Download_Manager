import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import java.net.*;

public class DownloadFrame extends JFrame implements Observer
{
  private JTextField addTextField;
  private DownloadTableModel tableModel;
  private JTable table;
  private JButton pauseButton, resumeButton, cancelButton, clearButton;
  private Download selectedDownload;
  private boolean clearing;

  public DownloadFrame()
  {
    setTitle("Download Manager");
    setSize(640,480);
    addWindowListener(new WindowAdapter(){
      public void windowClosing(WindowEvent e)
      {
        actionExit();
      }
    });

    JMenuBar menubar = new JMenuBar();
    JMenu filemenu = new JMenu("File");
    filemenu.setMnemonic(KeyEvent.VK_F);
    JMenuItem fileExitMenuItem = new JMenuItem("Exit",KeyEvent.VK_X);
    fileExitMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e)
      {
        actionExit();
      }
    });

    filemenu.add(fileExitMenuItem);
    menubar.add(filemenu);
    setJMenuBar(menubar);

    JPanel addPanel = new JPanel();
    addTextField = new JTextField(30);
    addPanel.add(addTextField);
    JButton addButton = new JButton("Add Download");
    addButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e)
      {
        actionAdd();
      }
    });
    addPanel.add(addButton);
    tableModel = new DownloadTableModel();
    table = new JTable(tableModel);
    table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e)
      {
        tableSelectionChanged();
      }
    });

    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    ProgressRenderer renderer = new ProgressRenderer(0,100);
    renderer.setStringPainted(true);
    table.setDefaultRenderer(JProgressBar.class,renderer);

    table.setRowHeight((int) renderer.getPreferredSize().getHeight());
    JPanel downloadspanel = new JPanel();
    downloadspanel.setBorder(BorderFactory.createTitledBorder("Downloads"));
    downloadspanel.setLayout(new BorderLayout());
    downloadspanel.add(new JScrollPane(table), BorderLayout.CENTER);

    JPanel buttonPanel =new JPanel();
    pauseButton = new JButton("Pause");
    pauseButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e)
      {
        actionPause();
      }
    });
    pauseButton.setEnabled(false);
    buttonPanel.add(pauseButton);

    resumeButton = new JButton("Resume");
    resumeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e)
      {
        actionResume();
      }
    });
    resumeButton.setEnabled(false);
    buttonPanel.add(resumeButton);

    cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e)
      {
        actionCancel();
      }
    });
    cancelButton.setEnabled(false);
    buttonPanel.add(cancelButton);

    clearButton = new JButton("Clear");
    clearButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e)
      {
        actionClear();
      }
    });
    clearButton.setEnabled(false);
    buttonPanel.add(clearButton);

    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(addPanel,BorderLayout.NORTH);
    getContentPane().add(downloadspanel,BorderLayout.CENTER);
    getContentPane().add(buttonPanel,BorderLayout.SOUTH);

  }

  private void actionExit()
  {
    System.exit(0);
  }

  private void actionAdd()
  {
    URL verifiedUrl = verifyUrl(addTextField.getText());
    if(verifiedUrl != null)
    {
      tableModel.addDownload(new  Download(verifiedUrl));
      addTextField.setText("");

    }else{
      JOptionPane.showMessageDialog(this,"Invalid Download URl","Error",JOptionPane.ERROR_MESSAGE);
    }
  }

  private URL verifyUrl(String url)
  {
    if(!url.toLowerCase().startsWith("http://"))
    {
      return null;
    }
    URL verifiedUrl = null;
    try{
      verifiedUrl = new URL(url);
    }catch(Exception e)
    {
      return null;
    }

    if(verifiedUrl.getFile().length() < 2)
    {
      return null;
    }
    return verifiedUrl;
  }

  private void tableSelectionChanged()
  {
    if(selectedDownload != null)
    {
      selectedDownload.deleteObserver(DownloadFrame.this);
    }

    if(!clearing && table.getSelectedRow() > -1)
    {
      selectedDownload = tableModel.getDownload(table.getSelectedRow());
      selectedDownload.addObserver(DownloadFrame.this);
      updateButtons();
    }
  }

  private void actionPause()
  {
    selectedDownload.pause();
    updateButtons();
  }

  private void actionResume()
  {
    selectedDownload.resume();
    updateButtons();
  }

  private void actionCancel()
  {
    selectedDownload.cancel();
    updateButtons();

  }

  private void actionClear()
  {
    clearing = true;
    tableModel.clearDownload(table.getSelectedRow());
    clearing = false;
    selectedDownload = null;
    updateButtons();
  }

  private void updateButtons()
  {
    if(selectedDownload != null)
    {
      int status = selectedDownload.getStatus();
      switch(status)
      {
        case Download.DOWNLOADING:
        pauseButton.setEnabled(true);
        resumeButton.setEnabled(false);
        cancelButton.setEnabled(true);
        clearButton.setEnabled(false);
        break;

        case Download.PAUSED:
        pauseButton.setEnabled(false);
        resumeButton.setEnabled(true);
        cancelButton.setEnabled(true);
        clearButton.setEnabled(false);
        break;

        case Download.ERROR:
        pauseButton.setEnabled(false);
        resumeButton.setEnabled(true);
        cancelButton.setEnabled(false);
        clearButton.setEnabled(true);
        break;

        default:
        pauseButton.setEnabled(false);
        resumeButton.setEnabled(false);
        cancelButton.setEnabled(false);
        clearButton.setEnabled(true);
        break;
      }

  }else{
    pauseButton.setEnabled(false);
    resumeButton.setEnabled(false);
    cancelButton.setEnabled(false);
    clearButton.setEnabled(false);

  }
}

public void update(Observable o,Object arg)
{
  if(selectedDownload != null && selectedDownload.equals(o))
  {
    updateButtons();
  }
}


public static void main(String args[])
{
  SwingUtilities.invokeLater(new Runnable() {
    public void run()
    {
      DownloadFrame frame = new DownloadFrame();
      frame.setVisible(true);
    }
  });
}

}
