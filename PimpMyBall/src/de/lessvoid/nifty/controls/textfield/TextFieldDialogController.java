package de.lessvoid.nifty.controls.textfield;

import java.util.Properties;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.Button;
import de.lessvoid.nifty.controls.ButtonClickedEvent;
import de.lessvoid.nifty.controls.CheckBox;
import de.lessvoid.nifty.controls.CheckBoxStateChangedEvent;
import de.lessvoid.nifty.controls.Controller;
import de.lessvoid.nifty.controls.Label;
import de.lessvoid.nifty.controls.TextField;
import de.lessvoid.nifty.controls.TextFieldChangedEvent;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.tools.Color;
import de.lessvoid.xml.xpp3.Attributes;
import mygame.Main;

/**
 * The ListBoxDialog to show off the new ListBox and a couple of more new Nifty 1.3 things.
 * @author void
 */
public class TextFieldDialogController implements Controller {
  private TextField mainTextField;
  private TextField passwordMainTextField;
  private CheckBox passwordCharCheckBox;
  private TextField passwordCharTextField;
  private CheckBox maxLengthEnableCheckBox;
  private TextField maxLengthTextField;
  private Label textChangedLabel;
  private Label keyEventLabel;
  private Button logInButton;
  private Button cancelButton;
  
  @Override
  public void bind(
      final Nifty nifty,
      final Screen screen,
      final Element element,
      final Properties parameter,
      final Attributes controlDefinitionAttributes) {
    this.mainTextField = screen.findNiftyControl("mainTextField", TextField.class);
    this.passwordCharCheckBox = screen.findNiftyControl("passwordCharCheckBox", CheckBox.class);
    this.passwordCharTextField = screen.findNiftyControl("passwordCharTextField", TextField.class);
    this.passwordMainTextField = screen.findNiftyControl("passwordMainTextField", TextField.class);
    this.maxLengthEnableCheckBox = screen.findNiftyControl("maxLengthEnableCheckBox", CheckBox.class);
    this.maxLengthTextField = screen.findNiftyControl("maxLengthTextField", TextField.class);
    this.textChangedLabel = screen.findNiftyControl("textChangedLabel", Label.class);
    this.keyEventLabel = screen.findNiftyControl("keyEventLabel", Label.class);
    this.logInButton = screen.findNiftyControl("logInButton", Button.class);
    this.cancelButton = screen.findNiftyControl("logInButton", Button.class);
    
  }

  @Override
  public void init(final Properties parameter, final Attributes controlDefinitionAttributes) {
    //passwordCharTextField.setText("*");
    mainTextField.setText("alex");
    passwordMainTextField.setText("pass");
    //maxLengthTextField.setText("5");
    textChangedLabel.setText("---");
    keyEventLabel.setText("---");
    keyEventLabel.setColor(new Color("#ff0f"));
    setPasswordCharTextFieldEnableState();
    setMaxLengthFieldEnableState();
    logInButton.enable();
    cancelButton.enable();
  }

  @Override
  public void onStartScreen() {
  }

  @Override
  public void onFocus(final boolean getFocus) {
  }

  @Override
  public boolean inputEvent(final NiftyInputEvent inputEvent) {
    return false;
  }

  @NiftyEventSubscriber(id="passwordCharCheckBox")
  public void onPasswordCharCheckBoxChanged(final String id, final CheckBoxStateChangedEvent event) {
    setPasswordCharTextFieldEnableState();
  }

  @NiftyEventSubscriber(id="maxLengthEnableCheckBox")
  public void onMaxLengthEnableCheckBoxChanged(final String id, final CheckBoxStateChangedEvent event) {
    setMaxLengthFieldEnableState();
  }

  @NiftyEventSubscriber(id="passwordCharTextField")
  public void onPasswordCharTextFieldChanged(final String id, final TextFieldChangedEvent event) {
    updatePasswordChar();
  }

  @NiftyEventSubscriber(id="mainTextField")
  public void onTextChanged(final String id, final TextFieldChangedEvent event) {
    textChangedLabel.setText(event.getText());
  }
  
  @NiftyEventSubscriber(id="passwordMainTextField")
  public void onPasswordTextChanged(final String id, final TextFieldChangedEvent event) {
    //textChangedLabel.setText(event.getText());
      password = passwordMainTextField.getText();
  }

  @NiftyEventSubscriber(id="maxLengthTextField")
  public void onMaxLengthTextChanged(final String id, final TextFieldChangedEvent event) {
    setMaxLength(event.getText());
  }

  private void setMaxLength(final String text) {
    try {
      mainTextField.setMaxLength(Integer.valueOf(text));
    } catch (Exception e) {
    }
  }

  @NiftyEventSubscriber(id="mainTextField")
  public void onTextChanged(final String id, final NiftyInputEvent event) {
    keyEventLabel.setText(event.toString() + " [" + event.getCharacter() + "]");
  }
  
  @NiftyEventSubscriber(id="passwordMainTextField")
  public void onPasswordTextChanged(final String id, final NiftyInputEvent event) {
    //keyEventLabel.setText(event.toString() + " [" + event.getCharacter() + "]");
  }

  private void setPasswordCharTextFieldEnableState() {
    passwordCharTextField.setEnabled(passwordCharCheckBox.isChecked());
    updatePasswordChar();
  }

  private void updatePasswordChar() {
    if (passwordCharCheckBox.isChecked()) {
      if (passwordCharTextField.getText().isEmpty()) {
        mainTextField.disablePasswordChar();
      } else {
        //mainTextField.enablePasswordChar(passwordCharTextField.getText().charAt(0));
        //mainTextField.enablePasswordChar(passwordCharTextField2.getText().charAt(0));
      }
    } else {
      //mainTextField.disablePasswordChar();
    }
  }

  private void setMaxLengthFieldEnableState() {
    maxLengthTextField.setEnabled(maxLengthEnableCheckBox.isChecked());
    if (maxLengthEnableCheckBox.isChecked()) {
      setMaxLength(maxLengthTextField.getText());
    } else {
      mainTextField.setMaxLength(-1);
    }
  }
  
  public static Main haxx;
  String username;
  String password;
  @NiftyEventSubscriber(id="logInButton")
  public void onLogInButtonClicked(final String id, final ButtonClickedEvent event) {
    haxx.sendLogin(mainTextField.getText(), passwordMainTextField.getText());
  }
  
  @NiftyEventSubscriber(id="cancelButton")
  public void onCancelButtonClicked(final String id, final ButtonClickedEvent event) {
    // Exit on close.
  }
  
}

