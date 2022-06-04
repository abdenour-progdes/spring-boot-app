package com.FATCA.API.table;

import com.FATCA.API.controllers.XmlGen.CsvService;
import com.FATCA.API.history.History;
import com.FATCA.API.history.HistoryService;
import com.FATCA.API.user.AppUser;
import com.FATCA.API.user.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor

public class DataTableService  {
    //add a table
    //remove a table
    //get a table
    //get all tables
    //update a table
    private final DataTableRepo dataTableRepo;
    private final UserRepo userRepo;
    private final CsvService csvService;
    private final HistoryService historyService;

    public List<DataTable> getAllTables(){
        return dataTableRepo.findAll();
    }
    public List<DataTable> getUserTables(Long id){
        AppUser owner = userRepo.getById(id);
        System.out.println(dataTableRepo.findByOwner(owner).get(0));
       return  dataTableRepo.findByOwner(owner);
    }
    public DataTable addTable(DataTable table) throws Exception {
        AppUser owner = userRepo.findByUsername(table.getOwner().getUsername());
        if (owner != null){
            return dataTableRepo.save(table);
        }else {
          throw new Exception("user not found in the database");
        }
    }
    public void removeTable(Long id){
        Optional<DataTable> table = dataTableRepo.findById(id);
        table.ifPresent(dataTableRepo::delete);
    }

    public ArrayList<String[]> csvToArray(MultipartFile file){
        return (ArrayList<String[]>) csvService.getCsvFile(file);
    }
    public DataTable updateTable(Long id, List<String[]> data, String updateMessage, Long userId){
        DataTable table = dataTableRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("table not found for this id :: " + id));
        AppUser owner = userRepo.getById(userId);
        historyService.saveHistory(new History(updateMessage, owner, table));

        table.setData((ArrayList<String[]>) data);

        return dataTableRepo.save(table);
    }
}
