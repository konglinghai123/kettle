package net.oschina.kettleutil.jobentry;

import cn.benma666.myutils.StringUtil;
import net.oschina.kettleutil.common.KuConstInterface;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryDialogInterface;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.StyledTextComp;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.job.dialog.JobDialog;
import org.pentaho.di.ui.job.entry.JobEntryDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class JobEntryKettleUtilDialog extends JobEntryDialog implements JobEntryDialogInterface {
    private static Class<?> PKG = JobEntryKettleUtil.class;
    private boolean changed;
    private FormData fdClassName;
    private FormData fdConfigInfo;
    private FormData fdName;
    private FormData fdlClassName;
    private FormData fdlConfigInfo;
    private FormData fdlName;
    private FormData fdlPosition;
    private JobEntryKettleUtil jobEntry;
    private Listener lsCancel;
    private SelectionAdapter lsDef;
    private Listener lsGet;
    private Listener lsOK;
    private Shell shell;
    private Button wCancel;
    private TextVar wClassName;
    private StyledTextComp wConfigInfo;
    private Button wGet;
    private Text wName;
    private Button wOK;
    private Label wlClassName;
    private Label wlConfigInfo;
    private Label wlName;
    private Label wlPosition;

    public JobEntryKettleUtilDialog(Shell parent, JobEntryInterface jobEntryInt, Repository rep, JobMeta jobMeta) {
        super(parent, jobEntryInt, rep, jobMeta);
        this.jobEntry = (JobEntryKettleUtil) jobEntryInt;
        if (this.jobEntry.getName() == null) {
            this.jobEntry.setName(BaseMessages.getString(PKG, "JobEntryKettleUtil.Name.Default", new String[0]));
        }
    }

    public JobEntryInterface open() {
        Shell parent = getParent();
        Display display = parent.getDisplay();
        this.shell = new Shell(parent, this.props.getJobsDialogStyle());
        this.props.setLook(this.shell);
        JobDialog.setShellImage(this.shell, this.jobEntry);
        ModifyListener lsMod = new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                JobEntryKettleUtilDialog.this.jobEntry.setChanged();
            }
        };
        this.changed = this.jobEntry.hasChanged();
        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = 5;
        formLayout.marginHeight = 5;
        this.shell.setLayout(formLayout);
        this.shell.setText(BaseMessages.getString(PKG, "JobEntryKettleUtil.Title", new String[0]));
        int middle = this.props.getMiddlePct();
        this.wGet = new Button(this.shell, 8);
        this.wGet.setText("获取默认配置");
        this.wGet.setToolTipText("在输入类名称后再通过此按钮获取对应默认配置信息");
        this.wOK = new Button(this.shell, 8);
        this.wOK.setText(BaseMessages.getString(PKG, "System.Button.OK", new String[0]));
        this.wCancel = new Button(this.shell, 8);
        this.wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel", new String[0]));
        BaseStepDialog.positionBottomButtons(this.shell, new Button[]{this.wOK, this.wCancel, this.wGet}, 4, null);
        this.wlName = new Label(this.shell, 131072);
        this.wlName.setText(BaseMessages.getString(PKG, "JobEntryKettleUtil.Jobname.Label", new String[0]));
        this.props.setLook(this.wlName);
        this.fdlName = new FormData();
        this.fdlName.left = new FormAttachment(0, 0);
        this.fdlName.right = new FormAttachment(middle, -4);
        this.fdlName.top = new FormAttachment(0, 4);
        this.wlName.setLayoutData(this.fdlName);
        this.wName = new Text(this.shell, 18436);
        this.props.setLook(this.wName);
        this.wName.addModifyListener(lsMod);
        this.fdName = new FormData();
        this.fdName.left = new FormAttachment(middle, 0);
        this.fdName.top = new FormAttachment(0, 4);
        this.fdName.right = new FormAttachment(100, 0);
        this.wName.setLayoutData(this.fdName);
        this.wlClassName = new Label(this.shell, 131072);
        this.wlClassName.setText(BaseMessages.getString(PKG, "JobEntryKettleUtil.ClassName.Label", new String[0]) + " ");
        this.props.setLook(this.wlClassName);
        this.fdlClassName = new FormData();
        this.fdlClassName.left = new FormAttachment(0, 0);
        this.fdlClassName.right = new FormAttachment(middle, -4);
        this.fdlClassName.top = new FormAttachment(this.wName, 4);
        this.wlClassName.setLayoutData(this.fdlClassName);
        this.wClassName = new TextVar(this.jobEntry, this.shell, 18436);
        this.props.setLook(this.wClassName);
        this.wClassName.addModifyListener(lsMod);
        this.fdClassName = new FormData();
        this.fdClassName.left = new FormAttachment(middle, 0);
        this.fdClassName.top = new FormAttachment(this.wName, 4);
        this.fdClassName.right = new FormAttachment(100, 4);
        this.wClassName.setLayoutData(this.fdClassName);
        this.wlPosition = new Label(this.shell, 0);
        this.wlPosition.setText(BaseMessages.getString(PKG, "JobEntryKettleUtil.LineNr.Label", new String[]{KuConstInterface.SCHEDULER_TYPE_NOT_TIMING}));
        this.props.setLook(this.wlPosition);
        this.fdlPosition = new FormData();
        this.fdlPosition.left = new FormAttachment(0, 0);
        this.fdlPosition.bottom = new FormAttachment(this.wOK, -4);
        this.wlPosition.setLayoutData(this.fdlPosition);
        this.wlConfigInfo = new Label(this.shell, 0);
        this.wlConfigInfo.setText(BaseMessages.getString(PKG, "JobEntryKettleUtil.Script.Label", new String[0]));
        this.props.setLook(this.wlConfigInfo);
        this.fdlConfigInfo = new FormData();
        this.fdlConfigInfo.left = new FormAttachment(0, 0);
        this.fdlConfigInfo.top = new FormAttachment(this.wClassName, 4);
        this.wlConfigInfo.setLayoutData(this.fdlConfigInfo);
        this.wConfigInfo = new StyledTextComp(this.jobEntry, this.shell, 19202, "");
        this.wConfigInfo.setText(BaseMessages.getString(PKG, "JobEntryKettleUtil.Script.Default", new String[0]));
        this.props.setLook(this.wConfigInfo, 1);
        this.wConfigInfo.addModifyListener(lsMod);
        this.fdConfigInfo = new FormData();
        this.fdConfigInfo.left = new FormAttachment(0, 0);
        this.fdConfigInfo.top = new FormAttachment(this.wlConfigInfo, 4);
        this.fdConfigInfo.right = new FormAttachment(100, -10);
        this.fdConfigInfo.bottom = new FormAttachment(this.wlPosition, -4);
        this.wConfigInfo.setLayoutData(this.fdConfigInfo);
        this.wConfigInfo.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent arg0) {
                JobEntryKettleUtilDialog.this.setPosition();
            }
        });
        this.wConfigInfo.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                JobEntryKettleUtilDialog.this.setPosition();
            }

            public void keyReleased(KeyEvent e) {
                JobEntryKettleUtilDialog.this.setPosition();
            }
        });
        this.wConfigInfo.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                JobEntryKettleUtilDialog.this.setPosition();
            }

            public void focusLost(FocusEvent e) {
                JobEntryKettleUtilDialog.this.setPosition();
            }
        });
        this.wConfigInfo.addMouseListener(new MouseAdapter() {
            public void mouseDoubleClick(MouseEvent e) {
                JobEntryKettleUtilDialog.this.setPosition();
            }

            public void mouseDown(MouseEvent e) {
                JobEntryKettleUtilDialog.this.setPosition();
            }

            public void mouseUp(MouseEvent e) {
                JobEntryKettleUtilDialog.this.setPosition();
            }
        });
        this.wConfigInfo.addModifyListener(lsMod);
        this.lsCancel = new Listener() {
            public void handleEvent(Event e) {
                JobEntryKettleUtilDialog.this.cancel();
            }
        };
        this.lsOK = new Listener() {
            public void handleEvent(Event e) {
                JobEntryKettleUtilDialog.this.ok();
            }
        };
        this.lsGet = new Listener() {
            public void handleEvent(Event e) {
                JobEntryKettleUtilDialog.this.jobEntry.setClassName(JobEntryKettleUtilDialog.this.wClassName.getText());
                JobEntryKettleUtilDialog.this.jobEntry.setConfigInfo(JobEntryKettleUtilDialog.this.wConfigInfo.getText());
                String conf = null;
                String msg = "获取默认配置失败，请输入正确的类名称";
                try {
                    conf = JobEntryKettleUtilDialog.this.jobEntry.getDefaultConfigInfo();
                } catch (Exception e1) {
                    msg = e1.getMessage();
                }
                if (StringUtil.isBlank(conf)) {
                    JobEntryKettleUtilDialog.this.wConfigInfo.setText("{}");
                    MessageBox mb = new MessageBox(JobEntryKettleUtilDialog.this.shell, 33);
                    mb.setMessage(msg);
                    mb.setText("错误");
                    mb.open();
                    return;
                }
                JobEntryKettleUtilDialog.this.wConfigInfo.setText(conf);
            }
        };
        this.wCancel.addListener(13, this.lsCancel);
        this.wOK.addListener(13, this.lsOK);
        this.wGet.addListener(13, this.lsGet);
        this.lsDef = new SelectionAdapter() {
            public void widgetDefaultSelected(SelectionEvent e) {
                JobEntryKettleUtilDialog.this.ok();
            }
        };
        this.wName.addSelectionListener(this.lsDef);
        this.shell.addShellListener(new ShellAdapter() {
            public void shellClosed(ShellEvent e) {
                JobEntryKettleUtilDialog.this.cancel();
            }
        });
        getData();
        BaseStepDialog.setSize(this.shell, 250, 250, false);
        this.shell.open();
        this.props.setDialogSize(this.shell, "JobEvalDialogSize");
        while (!this.shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        return this.jobEntry;
    }

    public void setPosition() {
        String scr = this.wConfigInfo.getText();
        int linenr = this.wConfigInfo.getLineAtOffset(this.wConfigInfo.getCaretOffset()) + 1;
        int posnr = this.wConfigInfo.getCaretOffset();
        int colnr = 0;
        while (posnr > 0 && scr.charAt(posnr - 1) != '\n' && scr.charAt(posnr - 1) != '\r') {
            posnr--;
            colnr++;
        }
        this.wlPosition.setText(BaseMessages.getString(PKG, "JobEntryKettleUtil.Position.Label", new String[]{"" + linenr, "" + colnr}));
    }

    public void dispose() {
        this.props.setScreen(new WindowProperty(this.shell));
        this.shell.dispose();
    }

    public void getData() {
        if (this.jobEntry.getName() != null) {
            this.wName.setText(this.jobEntry.getName());
        }
        if (this.jobEntry.getClassName() != null) {
            this.wClassName.setText(this.jobEntry.getClassName());
        }
        if (this.jobEntry.getConfigInfo() != null) {
            this.wConfigInfo.setText(this.jobEntry.getConfigInfo());
        }
        this.wName.selectAll();
        this.wName.setFocus();
    }

    private void cancel() {
        this.jobEntry.setChanged(this.changed);
        this.jobEntry = null;
        dispose();
    }

    private void ok() {
        if (Const.isEmpty(this.wName.getText())) {
            MessageBox mb = new MessageBox(this.shell, 33);
            mb.setText(BaseMessages.getString(PKG, "System.StepJobEntryNameMissing.Title", new String[0]));
            mb.setMessage(BaseMessages.getString(PKG, "System.JobEntryNameMissing.Msg", new String[0]));
            mb.open();
            return;
        }
        this.jobEntry.setName(this.wName.getText());
        this.jobEntry.setConfigInfo(this.wConfigInfo.getText());
        this.jobEntry.setClassName(this.wClassName.getText());
        dispose();
    }
}
