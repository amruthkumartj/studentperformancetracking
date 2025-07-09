document.addEventListener('DOMContentLoaded', function () {

/* ───────── HELPER FUNCTIONS (FIX) ───────── */
// These functions were missing, causing errors in the attendance module.
function qs(selector) { return document.querySelector(selector); }
function qsa(selector) { return document.querySelectorAll(selector); }
function show(el, display) {
  if (el) el.style.display = display ? 'block' : 'none';
}
// Overwrite the display style for flex containers
function showFlex(el, display) {
    if (el) el.style.display = display ? 'flex' : 'none';
}


/* ───────── SIDEBAR / NAV TOP‑LEVEL SECTIONS ───────── */
var sidebar           = qs('nav.sidebar');
var toggleBtn         = qs('.toggle');

var dashSection       = document.getElementById('dashboardSection');
var studentSection    = document.getElementById('studentFeatures');
var attendanceSection = document.getElementById('attendanceManagementSection');

var dashboardLink     = qs('a[href="facultyDashboard.jsp"]');
var manageLink        = document.getElementById('manageStudentsLink');
var attendanceLink    = document.getElementById('attendanceNavLink');

var topPanels         = [dashSection, studentSection, attendanceSection];
function showOnly(sec){
  topPanels.forEach(function(p){ if (p) p.style.display = (p === sec ? 'block':'none'); });
}

/* — toggle sidebar open/close — */
if (toggleBtn){
  toggleBtn.addEventListener('click', function () {
    sidebar.classList.toggle('close');
  });
}

/* — dropdown (<li class="has-dropdown">) handling — */
Array.prototype.forEach.call(
  qsa('.has-dropdown > a'),
  function (a){
    a.addEventListener('click', function(e){
      e.preventDefault();
      var li = a.closest('.has-dropdown');
      Array.prototype.forEach.call(
        qsa('.has-dropdown'),
        function(x){ if (x !== li) x.classList.remove('active'); }
      );
      li.classList.toggle('active');
    });
  }
);

/* — dashboard / students / attendance main nav — */
if (dashboardLink){
  dashboardLink.addEventListener('click', function(e){
    e.preventDefault(); showOnly(dashSection);
  });
}

if (manageLink){
  manageLink.addEventListener('click', function(e){
    e.preventDefault();
    showOnly(studentSection); showStudentFeature('');
    var dd = manageLink.closest('.has-dropdown');
    if (dd) dd.classList.remove('active');
    if (!sidebar.classList.contains('close')) sidebar.classList.add('close');
  });
}

if (attendanceLink){
  attendanceLink.addEventListener('click', function(e){
    e.preventDefault(); showOnly(attendanceSection); showAttendanceAction('');
  });
}

/* ───────── DARK‑MODE TOGGLE ───────── */
var modeSwitch = qs('.toggle-switch');
var modeText   = qs('.mode-text');
if (modeSwitch && modeText){
  modeSwitch.addEventListener('click', function(){
    document.body.classList.toggle('dark');
    modeText.textContent = document.body.classList.contains('dark') ? 'Light mode' : 'Dark mode';
  });
}

/* =====================================================
   ==========  STUDENT‑MANAGEMENT  CODE  ==============
   ===================================================== */

var studentSearchInput   = document.getElementById('studentSearchInput');
var courseFilterDropdown = document.getElementById('courseFilterDropdown');
var resetFiltersBtn      = document.getElementById('resetFiltersBtn');
var studentsTableBody    = document.getElementById('studentsTableBody');
var sortableHeaders      = qsa('#studentsTable th[data-sort-col]');

/* cards & sub‑panels */
window.showStudentFeature = function (which){
  var cards   = qs('#studentFeatures .feature-card-wrap');
  var addCard = document.getElementById('addStudentCard');
  var viewSec = document.getElementById('viewStudentSection');
  var todo    = document.getElementById('todoPlaceholder');

  [addCard,viewSec,todo].forEach(function(el){ show(el, false); });
  showFlex(cards, false);

  switch(which){
    case 'add'   : show(addCard, true);      break;
    case 'view'  : show(viewSec, true); loadStudentData(); break;
    case 'edit':
    case 'delete':
    case 'search': show(viewSec, true); loadStudentData(); break;
    default      : showFlex(cards, true);
  }
};

/* ----------  Add‑Student Form ---------- */
var pickerAdd    = document.getElementById('coursePickerAdd');
var fieldsBlock  = document.getElementById('studentFields');
if (pickerAdd){
  pickerAdd.addEventListener('change', function(){
    if (fieldsBlock) fieldsBlock.style.display = pickerAdd.value ? 'flex' : 'none';
  });
}

var cancelBtn       = document.getElementById('cancelAddBtn');
var addStudentForm  = document.getElementById('addStudentForm');
var messageContainer= document.getElementById('addStudentMessage');

if (cancelBtn){
  cancelBtn.addEventListener('click', function(){
    addStudentForm.reset();
    pickerAdd.selectedIndex = 0;
    show(fieldsBlock, false);
    show(messageContainer, false);
    showStudentFeature('');
  });
}

function validateStudentForm(){
  var sid = document.getElementById('studentId').value.trim();
  var sem = document.getElementById('semester' ).value.trim();
  if (!/^\d+$/.test(sid)){ alert('Student ID must be numeric'); return false; }
  if (!/^\d+$/.test(sem) || sem<1 || sem>6){ alert('Semester must be 1‑6'); return false; }
  return true;
}

if (addStudentForm){
  addStudentForm.addEventListener('submit', function(e){
    e.preventDefault(); if (!validateStudentForm()) return;

    if(messageContainer){
      messageContainer.innerHTML =
        '<div class="loading-message"><div class="spinner"></div> Saving student…</div>';
      show(messageContainer, true);
    }
    var start = Date.now();

    fetch('AddStudServlet', {
      method : 'POST',
      headers: {'Content-Type':'application/x-www-form-urlencoded'},
      body   : new URLSearchParams(new FormData(addStudentForm))
    })
    .then(function(r){return r.text();})
    .catch(function(){return 'Network / server error. Try again.';})
    .then(function(txt){
      var wait = Math.max(0,1500-(Date.now()-start));
      setTimeout(function(){
        var ok = txt.toLowerCase().indexOf('success')!==-1;
        var col= ok ? '#008000' : '#d8000c';
        if(messageContainer){
          messageContainer.innerHTML =
            '<div style="display:flex;align-items:center;gap:12px;font-weight:bold;'+
            'padding:10px;border-radius:6px;border:1px solid '+col+';'+
            'background:'+(ok?'#e7f9ed':'#ffe6e6')+';color:'+col+'">'+
              '<span style="font-size:24px">'+(ok?'✅':'❌')+'</span>'+txt+
            '</div>';
          if (ok){
            addStudentForm.reset(); pickerAdd.selectedIndex=0;
            show(fieldsBlock, false);
            setTimeout(function(){ show(messageContainer, false); }, 5000);
          }
        }
      },wait);
    });
  });
}

/* ----------  Table load / filter / sort  ---------- */
function loadStudentData(){
  if(studentSearchInput)   studentSearchInput.value='';
  if(courseFilterDropdown) courseFilterDropdown.value='';

  if(studentsTableBody){
    studentsTableBody.innerHTML =
      '<tr><td colspan="7" style="text-align:center;padding:20px;">Loading…</td></tr>';
  }

  fetch('GetStudentsServlet', {
    method:'POST', headers:{'Content-Type':'application/json'}, body:'{}'
  })
  .then(function(r){
    if(!r.ok) throw new Error('HTTP '+r.status); return r.json();
  })
  .then(function(data){
    if(!studentsTableBody) return;
    studentsTableBody.innerHTML = data.length ? '' :
      '<tr><td colspan="7" style="text-align:center;padding:20px;">No students found.</td></tr>';
    data.forEach(function(s){
      var tr=document.createElement('tr');
      tr.innerHTML =
        '<td>'+s.studentId+'</td><td>'+s.fullName+'</td><td>'+s.course+'</td>'+
        '<td>'+s.semester+'</td><td>'+s.email+'</td><td>'+s.phone+'</td>'+
        '<td><button class="btn-action-edit" data-id="'+s.studentId+'">Edit</button>'+
            '<button class="btn-action-delete" data-id="'+s.studentId+'">Delete</button></td>';
      var del=tr.querySelector('.btn-action-delete');
      del.addEventListener('click', function(){
        var id=this.dataset.id;
        if(!confirm('Delete student '+id+'?')) return;
        fetch('DeleteStudentServlet',{
          method:'POST',headers:{'Content-Type':'application/x-www-form-urlencoded'},
          body:'studentId='+encodeURIComponent(id)
        })
        .then(function(r){return r.json();})
        .then(function(res){
          if(res.status==='success'){ alert(res.message); tr.remove(); applyTableFilters(); }
          else alert(res.message||'Delete failed');
        }).catch(function(){alert('Network error');});
      });
      studentsTableBody.appendChild(tr);
    });
    applyTableFilters(); sortTable(0,'asc');
  })
  .catch(function(){
    if(studentsTableBody){
      studentsTableBody.innerHTML =
        '<tr><td colspan="7" style="text-align:center;padding:20px;color:red">Failed to load.</td></tr>';
    }
  });
}

function applyTableFilters(){
  var q = (studentSearchInput?studentSearchInput.value:'').toLowerCase().trim();
  var c = (courseFilterDropdown?courseFilterDropdown.value:'').toLowerCase();
  var visible=0;
  Array.prototype.forEach.call(studentsTableBody.querySelectorAll('tr'),function(row){
    var tds=row.querySelectorAll('td');
    if(tds.length!==7){ row.style.display='none'; return;}
    var matchSearch = !q || [0,1,4,5].some(function(i){
      return tds[i].textContent.toLowerCase().indexOf(q)!==-1;
    });
    var matchCourse = !c || tds[2].textContent.toLowerCase()===c;
    row.style.display = (matchSearch && matchCourse)?'table-row':'none';
    if(row.style.display==='table-row') visible++;
  });
  var noRow = document.getElementById('noStudentsFilteredMessage');
  if(!visible){
    if(!noRow){
      noRow=document.createElement('tr'); noRow.id='noStudentsFilteredMessage';
      noRow.innerHTML='<td colspan="7" style="text-align:center;padding:20px;color:grey">No matching students found.</td>';
      studentsTableBody.appendChild(noRow);
    }
    noRow.style.display='table-row';
  }else if(noRow) noRow.style.display='none';
}
if(studentSearchInput)   studentSearchInput.addEventListener('input',applyTableFilters);
if(courseFilterDropdown) courseFilterDropdown.addEventListener('change',applyTableFilters);
if(resetFiltersBtn)      resetFiltersBtn.addEventListener('click',function(){
  if(studentSearchInput) studentSearchInput.value='';
  if(courseFilterDropdown) courseFilterDropdown.value='';
  applyTableFilters();
});

Array.prototype.forEach.call(sortableHeaders,function(th){
  th.addEventListener('click',function(){
    var col = +th.dataset.sortCol;
    var dir = th.dataset.sortDir = (th.dataset.sortDir==='asc'?'desc':'asc');
    Array.prototype.forEach.call(sortableHeaders,function(h){
      h.classList.remove('sort-asc','sort-desc');
      var i=h.querySelector('.sort-icon');
      if(i){ i.classList.remove('fa-sort-up','fa-sort-down'); i.classList.add('fa-sort'); }
    });
    th.classList.add('sort-'+dir);
    var ic = th.querySelector('.sort-icon');
    if(ic){ ic.classList.remove('fa-sort'); ic.classList.add(dir==='asc'?'fa-sort-up':'fa-sort-down'); }
    sortTable(col,dir);
  });
});

function sortTable(col,dir){
  var rows = Array.prototype.slice.call(
      studentsTableBody.querySelectorAll('tr')).filter(function(r){
        return r.querySelectorAll('td').length===7;
      });
  rows.sort(function(a,b){
    var A=a.querySelectorAll('td')[col].textContent.trim();
    var B=b.querySelectorAll('td')[col].textContent.trim();
    var num = (col===0||col===3);
    var x=num? +A||0 : A.toLowerCase();
    var y=num? +B||0 : B.toLowerCase();
    return dir==='asc' ? (x>y?1:x<y?-1:0) : (x<y?1:x>y?-1:0);
  }).forEach(function(r){ studentsTableBody.appendChild(r); });
  applyTableFilters();
}

/* =====================================================
   ==========  ATTENDANCE  PANEL LOGIC  ================
   ===================================================== */
var SUBJECTS = {
		  'MCA'   : {1:['OOPs in Java','C Programming','Discrete Maths'],
		             2:['Data Structures','DBMS','Python'],
		             3:['Operating Systems','Computer Networks','Software Engg']},
		  'MBA'   : {1:['Managerial Eco','Accounting','Business Stats'],
		             2:['Marketing Mgmt','HR Mgmt','Financial Mgmt']},
		  'B.Tech': {1:['Physics','Maths‑I','C Programming'],
		             2:['Chemistry','Maths‑II','Data Structures']}
		};

		var progSel   = qs('#programSelect');
		var semSel    = qs('#semesterSelect');
		var subjSel   = qs('#subjectSelect');
		var attTimeIn = qs('#attDateTime');

		function fillSelect(sel, list, ph){
		  sel.innerHTML = '';
		  var phOpt = document.createElement('option');
		  phOpt.disabled = phOpt.selected = true; phOpt.textContent = ph;
		  sel.appendChild(phOpt);
		  list.forEach(function(txt){
		    var o=document.createElement('option'); o.textContent=txt; sel.appendChild(o);
		  });
		}
		function disable(sel, yes){ sel.disabled = yes; if(yes) sel.selectedIndex = 0; }

		if(progSel) progSel.addEventListener('change', function(){
		  var map = SUBJECTS[this.value] || {};
		  disable(semSel, false); fillSelect(semSel, Object.keys(map), '-- choose semester --');
		  disable(subjSel,true); subjSel.innerHTML='<option disabled selected>-- choose subject --</option>';
		});
		if(semSel) semSel.addEventListener('change', function(){
		  var list = (SUBJECTS[progSel.value]||{})[this.value] || [];
		  disable(subjSel,false); fillSelect(subjSel,list,'-- choose subject --');
		});

		/* live clock in read‑only input */
		function tick(){ if(attTimeIn) attTimeIn.value = new Date().toLocaleString(); }
		tick(); setInterval(tick,1000);

		/* Attendance panel switching */
		var attendCards = qs('#attendanceFeatureCards');
		var takeSec     = qs('#takeAttendanceSection');
		var viewSec     = qs('#viewAttendanceSection');
		window.showAttendanceAction = function(act){
          // FIX: The function 'show' was not defined. Using the new helper.
		  [takeSec, viewSec].forEach(function(x){ show(x, false); });
          showFlex(attendCards, false);

		  if (act==='take') show(takeSec, true);
		  else if(act==='view') show(viewSec, true);
		  else showFlex(attendCards, true);
		};

		/* “Load students” button logic */
		var loadBtn   = qs('#loadStudentsBtn');
		var studentsWrap; // container for the student list, created once

		if(loadBtn) loadBtn.addEventListener('click', function () {
          var formContainer = qs('#takeAttendanceSection'); // The parent of the form

		  /* validate selections */
		  if(!progSel.value || !semSel.value || !subjSel.value){
		    alert('Please choose Program, Semester and Subject first'); return;
		  }

		  /* hide form parts inside the container */
          var formParts = formContainer.querySelectorAll('.form-group, button');
		  formParts.forEach(function(el){ show(el, false); });

		  /* create list container first time only */
		  if(!studentsWrap){
		    studentsWrap = document.createElement('div');
		    studentsWrap.id = 'studentsWrap';
		    studentsWrap.style.marginTop = '30px';

		    studentsWrap.innerHTML =
		      '<div style="display:flex;align-items:center;margin-bottom:12px;">'+
		        '<input id="studentSearchAtt" placeholder="Search…" style="flex:1;padding:8px 10px;border:1px solid #ccc;border-radius:6px">'+
		        '<button id="attBackBtn" style="margin-left:12px;">Back</button></div>'+
		      '<div style="max-height:380px;overflow:auto;">'+
		        '<table id="attTable" style="width:100%;border-collapse:collapse">'+
		          '<thead><tr><th>ID</th><th>Name</th><th style="text-align:center">Present</th></tr></thead>'+
		          '<tbody></tbody></table></div>'+
		      '<button id="submitAttendanceBtn" style="margin-top:18px">Submit Attendance</button>';

		    formContainer.appendChild(studentsWrap);

            /* live search -- add listener only once */
            studentsWrap.querySelector('#studentSearchAtt').addEventListener('input', function(){
                var q=this.value.toLowerCase();
                qsa('#attTable tbody tr').forEach(function(r){
                    var txt = r.children[0].textContent.toLowerCase() + r.children[1].textContent.toLowerCase();
                    show(r, txt.indexOf(q)!==-1);
                });
            });
		  }
		  show(studentsWrap, true);

		  var tbody = qs('#attTable tbody');
		  tbody.innerHTML = '<tr><td colspan="3" style="text-align:center;padding:20px;">Loading…</td></tr>';

		  /* fetch students */
		  fetch('GetStudentsServlet', {method:'POST',headers:{'Content-Type':'application/json'},body:'{}'})
		    .then(function(r){return r.json();})
		    .then(function(list){
		      tbody.innerHTML = '';
		      list.forEach(function(s){
		        if(s.course !== progSel.value) return;
		        var tr = document.createElement('tr');
		        tr.innerHTML =
		          '<td>'+s.studentId+'</td><td>'+s.fullName+'</td>'+
		          '<td style="text-align:center"><label class="ios-switch">'+
		          '<input type="checkbox" class="attToggle" data-id="'+s.studentId+'" checked>'+
		          '<span class="slider round"></span></label></td>';
		        tbody.appendChild(tr);
		      });
		    })
		    .catch(function(){ tbody.innerHTML='<tr><td colspan="3" style="text-align:center;padding:20px;color:red">Failed to load students.</td></tr>'; });
		});

        /* Back & Submit buttons - Use Event Delegation on the document */
        document.addEventListener('click', function(e){
            // Back button
            if(e.target && e.target.id === 'attBackBtn'){
                var formContainer = qs('#takeAttendanceSection');
                var formParts = formContainer.querySelectorAll('.form-group, button');

                show(studentsWrap, false);
                formParts.forEach(function(el){
                    if(el.id !== 'studentsWrap' && !studentsWrap.contains(el)){
                       show(el,true);
                    }
                });
                progSel.selectedIndex = 0;
                disable(semSel,true);
                disable(subjSel,true);
            }

            // Submit button
            if(e.target && e.target.id === 'submitAttendanceBtn'){
                var toggles = qsa('#attTable .attToggle');
                if(!toggles.length){ alert('Nothing to submit'); return; }

                // FIX: Use Array.prototype.slice.call for ES5 compatibility with .map
                var records = Array.prototype.slice.call(toggles).map(function(cb){
                    return {studentId: cb.getAttribute('data-id'), status: cb.checked?'P':'A'};
                });

                fetch('AttendanceServlet',{
                    method :'POST', headers:{'Content-Type':'application/json'},
                    body   : JSON.stringify({
                    program : progSel.value,
                    semester: semSel.value,
                    subject : subjSel.value,
                    dateTime: attTimeIn.value,
                    records : records
                    })
                })
                .then(function(r){return r.text();})
                .then(function(msg){ alert(msg || 'Attendance saved'); showAttendanceAction(''); })
                .catch(function(){ alert('Network / server error'); });
            }
        });

		/* inject iOS‑style switch CSS once */
		var style=document.createElement('style');
		style.textContent =
		'.ios-switch{position:relative;display:inline-block;width:46px;height:26px}' +
		'.ios-switch input{opacity:0;width:0;height:0}' +
		'.slider{position:absolute;cursor:pointer;top:0;left:0;right:0;bottom:0;background:#ccc;transition:.3s;border-radius:26px}' +
		'.slider:before{position:absolute;content:"";height:22px;width:22px;left:2px;bottom:2px;background:#fff;transition:.3s;border-radius:50%}' +
		'input:checked+.slider{background:#4caf50}input:checked+.slider:before{transform:translateX(20px)}';
		document.head.appendChild(style);

		/* INITIAL VIEW */
		showOnly(dashSection);
});