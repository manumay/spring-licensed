package info.manuelmayer.licensed.tool;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import info.manuelmayer.licensed.io.LicenseReader;
import info.manuelmayer.licensed.io.LicenseReaderImpl;
import info.manuelmayer.licensed.model.License;
import info.manuelmayer.licensed.model.License.LicenseRestrictions;
import info.manuelmayer.licensed.model.LicenseImpl;
import info.manuelmayer.licensed.model.LicenseImpl.LicenseRestrictionsImpl;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class LicenseGeneratorAppController {
    
    @FXML
    private BorderPane pane;
    
    @FXML
    private TextField applicationKey;
    
    @FXML
    private TextField features;

    @FXML
    private TextField holder;
    
    @FXML
    private TextField issuer;
    
    @FXML
    private DatePicker issueDate;
    
    @FXML
    private TextField numberOfDevices;
    
    @FXML
    private TextField numberOfUsers;
    
    @FXML 
    private DatePicker validFrom;
    
    @FXML
    private DatePicker validTill;
    
    @FXML
    private TextField versions;
    
    @FXML
    private TextField hosts;
    
    @FXML
    private MenuItem save;
    
    @FXML
    private MenuItem saveAs;
    
    private Clock clock = Clock.systemDefaultZone();
    
    private File file;
    
    private BooleanProperty dirty = new SimpleBooleanProperty(false);
    
    @FXML
    public void initialize() {
        ChangeListener<Object> changeListener = new ChangeListener<Object>() {
            @Override
            public void changed(ObservableValue<? extends Object> observable, Object oldValue, Object newValue) {
                dirty.setValue(true);
            }
        };
        applicationKey.textProperty().addListener(changeListener);
        features.textProperty().addListener(changeListener);
        holder.textProperty().addListener(changeListener);
        issuer.textProperty().addListener(changeListener);
        issueDate.valueProperty().addListener(changeListener);
        validFrom.valueProperty().addListener(changeListener);
        validTill.valueProperty().addListener(changeListener);
        versions.textProperty().addListener(changeListener);
        hosts.textProperty().addListener(changeListener);
        
        numberOfDevices.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!StringUtils.isNumeric(newValue)) {
                    numberOfDevices.setText(oldValue);
                } else {
                    dirty.setValue(true);
                }
            }
        });
        
        numberOfUsers.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!StringUtils.isNumeric(newValue)) {
                    numberOfUsers.setText(oldValue);
                } else {
                    dirty.setValue(true);
                }
            }
        });
        
        dirty.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                updateTitle();
            }
        });
    }
        
    @FXML
    public void create() {
        if (!checkDirty()) {
            return;
        }
        
        this.file = null;
        
        enableControls();
        setDefaults();
        updateTitle();
    }
    
    @FXML
    public void open() {
        if (!checkDirty()) {
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(getInitialDirectory());
        File file = fileChooser.showOpenDialog(pane.getScene().getWindow());
        if (file != null) {
            LicenseReader r = new LicenseReaderImpl();
            License license = r.read(file);
            setLicense(license);
            setFile(file);
        }
    }
    
    @FXML
    public void save() {
        saveAs(file);
    }
    
    @FXML
    public void saveAs() {
        saveAs(null);
    }
    
    private void saveAs(File file) {
        checkValid();
        if (file == null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(getInitialDirectory());
            file = fileChooser.showSaveDialog(pane.getScene().getWindow());
        }
        if (file != null) {
            LicenseWriterImpl w = new LicenseWriterImpl();
            w.write(getLicense(), file);
            setFile(file);
        }
    }
    
    @FXML
    public void exit() {
        if (checkDirty()) {
            System.exit(0);
        }
    }
    
    @FXML
    public void about() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("APPlicensing");
        alert.setHeaderText("APPlicensing");
        alert.setContentText(getVersion());
        alert.showAndWait();
    }
    
    private void enableControls() {
    	applicationKey.disableProperty().set(false);
        features.disableProperty().set(false);
        holder.disableProperty().set(false);
        issuer.disableProperty().set(false);
        issueDate.disableProperty().set(false);
        numberOfUsers.disableProperty().set(false);
        numberOfDevices.disableProperty().set(false);
        validFrom.disableProperty().set(false);
        validTill.disableProperty().set(false);
        versions.disableProperty().set(false);
        hosts.disableProperty().set(false);
        
        save.disableProperty().set(false);
        saveAs.disableProperty().set(false);
    }
    
    private void setDefaults() {
    	applicationKey.setText("new-application");
        features.setText(".*");
        issuer.setText("DIG GmbH");
        holder.setText("");
        issueDate.setValue(LocalDate.now(clock));
        numberOfUsers.setText("9999");
        numberOfDevices.setText("9999");
        validFrom.setValue(LocalDate.now(clock).withDayOfYear(1));
        validTill.setValue(LocalDate.now(clock).withDayOfYear(1).plusYears(100));
        versions.setText(".*");
        hosts.setText(".*");
    }
    
    private License getLicense() {
        LicenseImpl details = new LicenseImpl();
        details.setApplicationKey(applicationKey.getText());
        details.setHolder(holder.getText());
        details.setIssueDate(issueDate.getValue());
        details.setIssuer(issuer.getText());
        
        LicenseRestrictionsImpl restrictions = details.getRestrictions();
        restrictions.setFeatures(Pattern.compile(features.getText()));
        restrictions.setHosts(Pattern.compile(hosts.getText()));
        restrictions.setNumberOfDevices(Integer.valueOf(numberOfDevices.getText()));
        restrictions.setNumberOfUsers(Integer.valueOf(numberOfUsers.getText()));
        restrictions.setValidFrom(validFrom.getValue());
        restrictions.setValidTill(validTill.getValue());
        restrictions.setVersions(Pattern.compile(versions.getText()));
        
        return details;
    }
    
    private void setLicense(License license) {
    	applicationKey.setText(license.getApplicationKey());
        holder.setText(license.getHolder());
        issuer.setText(license.getIssuer());
        issueDate.setValue(license.getIssueDate());
        
        LicenseRestrictions restrictions = license.getRestrictions();
        
        Pattern featurePattern = restrictions.getFeatures();
        features.setText(featurePattern != null ? featurePattern.pattern() : ".*");
        
        Pattern hostPattern = restrictions.getHosts();
        hosts.setText(hostPattern != null ? hostPattern.pattern() : ".*");
        
        numberOfDevices.setText(String.valueOf(restrictions.getNumberOfDevices()));
        numberOfUsers.setText(String.valueOf(restrictions.getNumberOfUsers()));
        validFrom.setValue(restrictions.getValidFrom());
        validTill.setValue(restrictions.getValidTill());
        
        Pattern versionPattern = restrictions.getVersions();
        versions.setText(versionPattern != null ? versionPattern.pattern() : ".*");
    }
    
    private void setFile(File file) {
        this.file = file;
        enableControls();
        dirty.setValue(false);
    }
    
    private boolean checkDirty() {
        if (dirty.get()) {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Änderungen verwerfen");
            alert.setHeaderText("Lizenzinformationen wurden geändert");
            alert.setContentText("Sollen die Änderungen verworfen werden?");

            Optional<ButtonType> result = alert.showAndWait();
            return result.get() == ButtonType.OK;
        }
        return true;
    }
    
    private boolean checkValid() {
        List<String> messages = validate();
        if (!messages.isEmpty()) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Warnung");
            alert.setHeaderText("Ein oder mehrere Eingaben sind ungültig.");
            alert.setContentText(messages.stream().collect(Collectors.joining("\r\n")));
            alert.showAndWait();
            return false;
        }
        return true;
    }

    private List<String> validate() {
        List<String> messages = new ArrayList<>();
        try {
            Pattern.compile(features.getText());
        } catch (PatternSyntaxException e) {
            messages.add("Der Wert im Feld Funktionen ist kein regulärer Ausdruck.");
        }
        try {
            Pattern.compile(versions.getText());
        } catch (PatternSyntaxException e) {
            messages.add("Der Wert im Feld Versionen ist kein regulärer Ausdruck.");
        }
        return messages;
    }
    
    private File getInitialDirectory() {
        String property = getProperty("initialdir");
        File initialDir = new File(property);
        if (initialDir.exists() && initialDir.isDirectory()) {
            return initialDir;
        }
        return null;
    }
    
    private String getVersion() {
        String property = getProperty("version");
        return StringUtils.isNotBlank(property) ? property : "Unbekannt";
    }
    
    private String getProperty(String property) {
        String path = "/licensing.properties";
        try (InputStream in = LicenseGeneratorAppController.class.getResourceAsStream(path)) {
            Properties props = new Properties();
            props.load(in);
            return (String) props.get(property);
        } catch (IOException e) {
            return "";
        }
    }
    
    private void updateTitle() {
        String title = "APPlicensing";
        title += " - " + (file != null ? file.getName() : "neu");
        if (dirty.get()) {
            title += "*";
        }
        ((Stage)pane.getScene().getWindow()).setTitle(title);
    }
}
