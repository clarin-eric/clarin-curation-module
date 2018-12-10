package eu.clarin.cmdi.curation.vlo_extensions;

import java.util.List;
import java.util.Map;

import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.importer.CMDIData;
import eu.clarin.cmdi.vlo.importer.CMDIDataFactory;
import eu.clarin.cmdi.vlo.importer.processor.ValueSet;

/*
* @author Wolfgang Walter SAUER (wowasa) &lt;wolfgang.sauer@oeaw.ac.at&gt;
*/
public class CMDIDataImplFactory implements CMDIDataFactory<Map<String,List<ValueSet>>> {
    private final FieldNameService fieldNameService;
    
    public CMDIDataImplFactory(FieldNameService fieldNameService) {
        this.fieldNameService = fieldNameService;
    }

    @Override
    public CMDIData<Map<String,List<ValueSet>>> newCMDIDataInstance() {
        // TODO Auto-generated method stub
        return new CMDIDataImpl(this.fieldNameService);
    }

}