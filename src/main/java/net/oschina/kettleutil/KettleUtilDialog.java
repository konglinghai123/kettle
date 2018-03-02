package net.oschina.kettleutil;

import cn.benma666.myutils.StringUtil;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.ui.core.widget.StyledTextComp;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class KettleUtilDialog extends BaseStepDialog implements StepDialogInterface {
    private static Class<?> PKG = KettleUtilMeta.class;
    private FormData fdClassName;
    private FormData fdConfigInfo;
    private FormData fdlClassName;
    private FormData fdlConfigInfo;
    private KettleUtilMeta input;
    private TextVar wClassName;
    private StyledTextComp wConfigInfo;
    private Label wlClassName;
    private Label wlConfigInfo;

    public KettleUtilDialog(Shell parent, Object in, TransMeta transMeta, String sname) {
        super(parent, (BaseStepMeta) in, transMeta, sname);
        this.input = (KettleUtilMeta) in;
    }

    public String open() {
        Shell parent = getParent();
        Display display = parent.getDisplay();
        this.shell = new Shell(parent, 3312);
        this.props.setLook(this.shell);
        setShellImage(this.shell, this.input);
        ModifyListener lsMod = new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                KettleUtilDialog.this.input.setChanged();
            }
        };
        this.changed = this.input.hasChanged();
        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = 5;
        formLayout.marginHeight = 5;
        this.shell.setLayout(formLayout);
        this.shell.setText(BaseMessages.getString(PKG, "KettleUtil.Shell.Title", new String[0]));
        int middle = this.props.getMiddlePct();
        this.wlStepname = new Label(this.shell, 131072);
        this.wlStepname.setText(BaseMessages.getString(PKG, "System.Label.StepName", new String[0]));
        this.props.setLook(this.wlStepname);
        this.fdlStepname = new FormData();
        this.fdlStepname.left = new FormAttachment(0, 0);
        this.fdlStepname.right = new FormAttachment(middle, -4);
        this.fdlStepname.top = new FormAttachment(0, 4);
        this.wlStepname.setLayoutData(this.fdlStepname);
        this.wStepname = new Text(this.shell, 18436);
        this.wStepname.setText(this.stepname);
        this.props.setLook(this.wStepname);
        this.wStepname.addModifyListener(lsMod);
        this.fdStepname = new FormData();
        this.fdStepname.left = new FormAttachment(middle, 0);
        this.fdStepname.top = new FormAttachment(0, 4);
        this.fdStepname.right = new FormAttachment(100, 0);
        this.wStepname.setLayoutData(this.fdStepname);
        this.wlClassName = new Label(this.shell, 131072);
        this.wlClassName.setText(BaseMessages.getString(PKG, "KettleUtil.ClassName.Label", new String[0]) + " ");
        this.props.setLook(this.wlClassName);
        this.fdlClassName = new FormData();
        this.fdlClassName.left = new FormAttachment(0, 0);
        this.fdlClassName.right = new FormAttachment(middle, -4);
        this.fdlClassName.top = new FormAttachment(this.wStepname, 4);
        this.wlClassName.setLayoutData(this.fdlClassName);
        this.wClassName = new TextVar(this.transMeta, this.shell, 18436);
        this.props.setLook(this.wClassName);
        this.wClassName.addModifyListener(lsMod);
        this.fdClassName = new FormData();
        this.fdClassName.left = new FormAttachment(middle, 0);
        this.fdClassName.top = new FormAttachment(this.wStepname, 4);
        this.fdClassName.right = new FormAttachment(100, 4);
        this.wClassName.setLayoutData(this.fdClassName);
        this.wlConfigInfo = new Label(this.shell, 0);
        this.wlConfigInfo.setText(BaseMessages.getString(PKG, "KettleUtil.ConfigInfo.Label", new String[0]) + " ");
        this.props.setLook(this.wlConfigInfo);
        this.fdlConfigInfo = new FormData();
        this.fdlConfigInfo.left = new FormAttachment(0, 0);
        this.fdlConfigInfo.top = new FormAttachment(this.wClassName, 4);
        this.fdlConfigInfo.right = new FormAttachment(middle, -4);
        this.wlConfigInfo.setLayoutData(this.fdlConfigInfo);
        this.wOK = new Button(this.shell, 8);
        this.wOK.setText(BaseMessages.getString(PKG, "System.Button.OK", new String[0]));
        this.wGet = new Button(this.shell, 8);
        this.wGet.setText("获取默认配置");
        this.wGet.setToolTipText("在输入类名称后再通过此按钮获取对应默认配置信息");
        this.wlConfigInfo = new Label(this.shell, 0);
        this.wlConfigInfo.setText(BaseMessages.getString(PKG, "JobEntryKettleUtil.Script.Label", new String[0]));
        this.props.setLook(this.wlConfigInfo);
        this.fdlConfigInfo = new FormData();
        this.fdlConfigInfo.left = new FormAttachment(0, 0);
        this.fdlConfigInfo.top = new FormAttachment(this.wClassName, 4);
        this.wlConfigInfo.setLayoutData(this.fdlConfigInfo);
        this.wConfigInfo = new StyledTextComp(this.transMeta, this.shell, 19202, "");
        this.wConfigInfo.setText(BaseMessages.getString(PKG, "JobEntryKettleUtil.Script.Default", new String[0]));
        this.props.setLook(this.wConfigInfo, 1);
        this.wConfigInfo.addModifyListener(lsMod);
        this.fdConfigInfo = new FormData();
        this.fdConfigInfo.left = new FormAttachment(0, 0);
        this.fdConfigInfo.top = new FormAttachment(this.wlConfigInfo, 4);
        this.fdConfigInfo.right = new FormAttachment(100, -10);
        this.fdConfigInfo.bottom = new FormAttachment(this.wOK, -4);
        this.wConfigInfo.setLayoutData(this.fdConfigInfo);
        this.wCancel = new Button(this.shell, 8);
        this.wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel", new String[0]));
        BaseStepDialog.positionBottomButtons(this.shell, new Button[]{this.wOK, this.wCancel, this.wGet}, 4, null);
        this.lsCancel = new Listener() {
            public void handleEvent(Event e) {
                KettleUtilDialog.this.cancel();
            }
        };
        this.lsOK = new Listener() {
            public void handleEvent(Event e) {
                KettleUtilDialog.this.ok();
            }
        };
        this.lsGet = new Listener() {
            public void handleEvent(Event e) {
                KettleUtilDialog.this.input.setClassName(KettleUtilDialog.this.wClassName.getText());
                KettleUtilDialog.this.input.setConfigInfo(KettleUtilDialog.this.wConfigInfo.getText());
                String conf = null;
                String msg = "获取默认配置失败，请输入正确的类名称";
                try {
                    conf = KettleUtilDialog.this.input.getDefaultConfigInfo(KettleUtilDialog.this.transMeta, KettleUtilDialog.this.wStepname.getText(), KettleUtilDialog.variables);
                } catch (Exception e1) {
                    msg = e1.getMessage();
                }
                if (StringUtil.isBlank(conf)) {
                    KettleUtilDialog.this.wConfigInfo.setText("{}");
                    MessageBox mb = new MessageBox(KettleUtilDialog.this.shell, 33);
                    mb.setMessage(msg);
                    mb.setText("错误");
                    mb.open();
                    return;
                }
                KettleUtilDialog.this.wConfigInfo.setText(conf);
            }
        };
        this.wCancel.addListener(13, this.lsCancel);
        this.wOK.addListener(13, this.lsOK);
        this.wGet.addListener(13, this.lsGet);
        this.lsDef = new SelectionAdapter() {
            public void widgetDefaultSelected(SelectionEvent e) {
                KettleUtilDialog.this.ok();
            }
        };
        this.wStepname.addSelectionListener(this.lsDef);
        this.wClassName.addSelectionListener(this.lsDef);
        this.shell.addShellListener(new ShellAdapter() {
            public void shellClosed(ShellEvent e) {
                KettleUtilDialog.this.cancel();
            }
        });
        setSize();
        getData();
        this.input.setChanged(this.changed);
        this.shell.open();
        while (!this.shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        return this.stepname;
    }

    public void getData() {
        this.wStepname.selectAll();
        this.wClassName.setText(this.input.getClassName());
        this.wConfigInfo.setText(this.input.getConfigInfo());
    }

    private void cancel() {
        this.stepname = null;
        this.input.setChanged(this.changed);
        dispose();
    }

    private void ok() {
        this.stepname = this.wStepname.getText();
        this.input.setClassName(this.wClassName.getText());
        this.input.setConfigInfo(this.wConfigInfo.getText());
        dispose();
    }
}
